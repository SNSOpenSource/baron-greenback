package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;

import static com.googlecode.barongreenback.crawler.SubfeedDatasource.dataSource;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.UNIQUE;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class SubfeedJobCreator {
    public static Pair<Sequence<Record>, Sequence<StagedJob<Response>>> process(HttpDataSource dataSource, Definition destination, Sequence<Record> records) {
        return Pair.pair(
                mergePreviousUniqueIdentifiers(records, dataSource),
                createSubfeedJobs(records, destination, dataSourceUniques(dataSource)));
    }

    public static Sequence<StagedJob<Response>> createSubfeedJobs(Sequence<Record> records, Definition destination, Sequence<Pair<Keyword<?>, Object>> uniques) {
        return records.flatMap(subfeedsKeywords(destination, uniques));
    }

    private static Sequence<Record> mergePreviousUniqueIdentifiers(Sequence<Record> records, final HttpDataSource dataSource) {
        return records.map(new Callable1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return record(record.fields().join(dataSourceUniques(dataSource)));
            }
        });
    }

    private static Sequence<Pair<Keyword<?>, Object>> dataSourceUniques(HttpDataSource dataSource) {
        if (dataSource instanceof SubfeedDatasource) {
            return ((SubfeedDatasource) dataSource).uniqueIdentifiers();
        }
        return Sequences.sequence();
    }

    private static Callable1<Record, Sequence<StagedJob<Response>>> subfeedsKeywords(final Definition destination, final Sequence<Pair<Keyword<?>, Object>> uniques) {
        return new Callable1<Record, Sequence<StagedJob<Response>>>() {
            public Sequence<StagedJob<Response>> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(record, destination, uniques));
            }
        };
    }

    private static Callable1<Keyword<?>, StagedJob<Response>> toJob(final Record record, final Definition destination, final Sequence<Pair<Keyword<?>, Object>> uniques) {
        return new Callable1<Keyword<?>, StagedJob<Response>>() {
            @Override
            public HttpJob call(Keyword<?> keyword) throws Exception {
                return job(keyword, record, destination, uniques);
            }
        };
    }

    private static HttpJob job(Keyword<?> subfeedKeyword, Record record, Definition destination, Sequence<Pair<Keyword<?>, Object>> uniques) {
        Object value = record.get(subfeedKeyword);
        Uri uri = Uri.uri(value.toString());

        Sequence<Pair<Keyword<?>, Object>> keysAndValues = uniques.join(record.fields().filter(where(Callables.<Keyword<?>>first(), where(metadata(UNIQUE), is(true))))).realise();

        return HttpJob.job(dataSource(uri, subfeedKeyword.metadata().get(RECORD_DEFINITION).definition(), keysAndValues), destination);
    }
}