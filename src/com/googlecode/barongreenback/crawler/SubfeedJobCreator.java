package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.barongreenback.crawler.StagedJob.functions.datasource;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;

public class SubfeedJobCreator {
    private final HttpDatasource parentDatasource;
    private final Definition destination;

    public SubfeedJobCreator(HttpDatasource parentDatasource, Definition destination) {
        this.parentDatasource = parentDatasource;
        this.destination = destination;
    }

    public Pair<Sequence<Record>, Sequence<StagedJob>> process(Sequence<Record> records) {
        return Pair.pair(records.map(merge(parentDatasource.record())), createSubfeedJobs(records));
    }

    private Sequence<StagedJob> createSubfeedJobs(Sequence<Record> records) {
        return records.flatMap(subfeedsKeywords()).unique(datasource()).realise();
    }

    private Callable1<Record, Sequence<StagedJob>> subfeedsKeywords() {
        return new Callable1<Record, Sequence<StagedJob>>() {
            public Sequence<StagedJob> call(final Record record) throws Exception {
                Sequence<Pair<Keyword<?>, Object>> subfeeds = record.fields().filter(where(Callables.<Keyword<?>>first(), where(metadata(RECORD_DEFINITION), is(Predicates.notNullValue()))));

                return subfeeds.map(toJob(record));
            }
        };
    }

    private Callable1<Pair<Keyword<?>, Object>, StagedJob> toJob(final Record record) {
        return new Callable1<Pair<Keyword<?>, Object>, StagedJob>() {
            @Override
            public HttpJob call(Pair<Keyword<?>, Object> subfeedField) throws Exception {
                return job(subfeedField, record);
            }
        };
    }

    private HttpJob job(Pair<Keyword<?>, Object> subfeedField, Record record) {
        Uri uri = Uri.uri(subfeedField.second().toString());
        Record newRecord = one(record).map(merge(parentDatasource.record())).head();
        Definition subfeedDefinition = subfeedField.first().metadata().get(RECORD_DEFINITION).definition();

        return HttpJob.job(SubfeedDatasource.datasource(uri, parentDatasource.crawlerId(), subfeedDefinition, newRecord), destination);
    }
}