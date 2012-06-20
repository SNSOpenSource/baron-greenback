package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;

import static com.googlecode.barongreenback.crawler.SubfeedDatasource.dataSource;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.UNIQUE;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class SubfeedJobCreator {
    public static Sequence<HttpJob> createSubfeedJobs(Sequence<Record> records, Definition destination, Sequence<Pair<Keyword<?>, Object>> uniques) {
        return records.flatMap(subfeedsKeywords(destination, uniques));
    }

    private static Callable1<Record, Sequence<HttpJob>> subfeedsKeywords(final Definition destination, final Sequence<Pair<Keyword<?>, Object>> uniques) {
        return new Callable1<Record, Sequence<HttpJob>>() {
            public Sequence<HttpJob> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(record, destination, uniques));
            }
        };
    }

    private static Callable1<Keyword<?>, HttpJob> toJob(final Record record, final Definition destination, final Sequence<Pair<Keyword<?>, Object>> uniques) {
        return new Callable1<Keyword<?>, HttpJob>() {
            @Override
            public HttpJob call(Keyword<?> keyword) throws Exception {
                return job(keyword, record, destination, uniques);
            }
        };
    }

    public static HttpJob job(Keyword<?> subfeedKeyword, Record record, Definition destination, Sequence<Pair<Keyword<?>, Object>> uniques) {
        Object value = record.get(subfeedKeyword);
        Uri uri = Uri.uri(value.toString());

        Sequence<Pair<Keyword<?>, Object>> keysAndValues = uniques.join(record.fields().filter(where(Callables.<Keyword<?>>first(), where(metadata(UNIQUE), is(true))))).realise();

        return HttpJob.job(dataSource(uri, subfeedKeyword.metadata().get(RECORD_DEFINITION).definition(), keysAndValues), destination);
    }
}