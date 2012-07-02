package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class SubfeedJobCreator {
    public static Pair<Sequence<Record>, Sequence<StagedJob>> process(Container container, HttpDatasource datasource, Definition destination, Sequence<Record> records) {
        return Pair.pair(
                mergePreviousData(records, datasource),
                createSubfeedJobs(container, records, destination, datasource.data()));
    }

    public static Sequence<StagedJob> createSubfeedJobs(final Container container, Sequence<Record> records, Definition destination, Sequence<Pair<Keyword<?>, Object>> data) {
        return records.flatMap(subfeedsKeywords(container, destination, data)).unique(uri()).realise();
    }

    private static Sequence<Record> mergePreviousData(Sequence<Record> records, final HttpDatasource datasource) {
        return records.map(new Callable1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return record(datasource.data().join(record.fields()));
            }
        });
    }

    private static Callable1<Record, Sequence<StagedJob>> subfeedsKeywords(final Container container, final Definition destination, final Sequence<Pair<Keyword<?>, Object>> data) {
        return new Callable1<Record, Sequence<StagedJob>>() {
            public Sequence<StagedJob> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(container, record, destination, data));
            }
        };
    }

    private static Callable1<StagedJob, Uri> uri() {
        return new Callable1<StagedJob, Uri>() {
            @Override
            public Uri call(StagedJob job) throws Exception {
                return job.dataSource().uri();
            }
        };
    }

    private static Callable1<Keyword<?>, StagedJob> toJob(final Container container, final Record record, final Definition destination, final Sequence<Pair<Keyword<?>, Object>> data) {
        return new Callable1<Keyword<?>, StagedJob>() {
            @Override
            public HttpJob call(Keyword<?> subfeedKeyword) throws Exception {
                return job(container, subfeedKeyword, record, destination, data);
            }
        };
    }

    private static HttpJob job(Container container, Keyword<?> subfeedKeyword, Record record, Definition destination, Sequence<Pair<Keyword<?>, Object>> data) {
        Object subfeed = record.get(subfeedKeyword);
        Uri uri = Uri.uri(subfeed.toString());

        Sequence<Pair<Keyword<?>, Object>> keysAndValues = data.join(record.fields()).realise();

        return HttpJob.job(container, SubfeedDatasource.datasource(uri, subfeedKeyword.metadata().get(RECORD_DEFINITION).definition(), keysAndValues), destination);
    }
}