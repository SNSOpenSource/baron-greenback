package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.yadic.Container;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class SubfeedJobCreator {
    private final Container container;
    private final HttpDatasource parentDatasource;
    private final Definition destination;

    public SubfeedJobCreator(Container container, HttpDatasource parentDatasource, Definition destination) {
        this.container = container;
        this.parentDatasource = parentDatasource;
        this.destination = destination;
    }

    public Pair<Sequence<Record>, Sequence<StagedJob>> process(Sequence<Record> records) {
        return Pair.pair(merge(records), createSubfeedJobs(records));
    }

    private Sequence<StagedJob> createSubfeedJobs(Sequence<Record> records) {
        return records.flatMap(subfeedsKeywords(this.destination)).unique(datasource()).realise();
    }

    private Sequence<Record> merge(Sequence<Record> records) {
        return records.map(new Callable1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return record(parentDatasource.data().join(record.fields()));
            }
        });
    }

    private Callable1<Record, Sequence<StagedJob>> subfeedsKeywords(final Definition destination) {
        return new Callable1<Record, Sequence<StagedJob>>() {
            public Sequence<StagedJob> call(final Record record) throws Exception {
                Sequence<Keyword<?>> subfeedKeywords = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))).realise();
                return subfeedKeywords.map(toJob(record, destination));
            }
        };
    }

    private Callable1<StagedJob, HttpDatasource> datasource() {
        return new Callable1<StagedJob, HttpDatasource>() {
            @Override
            public HttpDatasource call(StagedJob job) throws Exception {
                return job.datasource();
            }
        };
    }

    private Callable1<Keyword<?>, StagedJob> toJob(final Record record, final Definition destination) {
        return new Callable1<Keyword<?>, StagedJob>() {
            @Override
            public HttpJob call(Keyword<?> subfeedKeyword) throws Exception {
                return job(subfeedKeyword, record, destination);
            }
        };
    }

    private HttpJob job(Keyword<?> subfeedKeyword, Record record, Definition destination) {
        Object subfeed = record.get(subfeedKeyword);
        Uri uri = Uri.uri(subfeed.toString());

        Sequence<Pair<Keyword<?>, Object>> keysAndValues = parentDatasource.data().join(record.fields()).realise();

        return HttpJob.job(container, SubfeedDatasource.datasource(uri, subfeedKeyword.metadata().get(RECORD_DEFINITION).definition(), keysAndValues), destination);
    }
}