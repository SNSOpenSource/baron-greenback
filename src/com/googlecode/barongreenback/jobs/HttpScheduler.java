package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;

public class HttpScheduler {
    public static final Keyword<String> SCHEDULED_REQUESTS = keyword("scheduledRequests", String.class);

    public static final Keyword<String> JOB_ID = keyword("scheduledRequests_id", String.class);
    public static final Keyword<String> REQUEST = keyword("request", String.class);
    public static final Keyword<Long> INTERVAL = keyword("delay", Long.class);

    private final Records records;
    private final Scheduler scheduler;
    private final Application application;

    public HttpScheduler(final Records records, final Scheduler scheduler, final Application application) {
        this.records = records;
        this.scheduler = scheduler;
        this.application = application;
        records.define(SCHEDULED_REQUESTS, JOB_ID, REQUEST, INTERVAL);
    }

    public String schedule(Record possiblePartialRecord) {
        records.put(SCHEDULED_REQUESTS, update(using(JOB_ID), possiblePartialRecord));
        String id = possiblePartialRecord.get(JOB_ID);

        Record fullRecord = job(id).get();
        scheduler.schedule(id, httpTask(parseRequest(fullRecord.get(REQUEST))), fullRecord.get(INTERVAL));
        return id;
    }

    public void start() {
        jobs().each(schedule());
    }

    public void stop() {
        jobs().each(cancel());
    }

    public void remove(String id) {
        scheduler.cancel(id);
        records.remove(SCHEDULED_REQUESTS, where(JOB_ID, is(id)));
    }

    public Sequence<Record> jobs() {
        return records.get(SCHEDULED_REQUESTS);
    }

    public Option<Record> job(String id) {
        return records.get(SCHEDULED_REQUESTS).find(where(JOB_ID, is(id)));
    }

    private Callable<Response> httpTask(final Request request) {
        return new Callable<Response>() {
            public Response call() throws Exception {
                return application.handle(request);
            }
        };
    }

    private Callable1<Record, Void> cancel() {
        return new Callable1<Record, Void>() {
            public Void call(Record record) throws Exception {
                scheduler.cancel(record.get(JOB_ID));
                return VOID;
            }
        };
    }

    private Callable1<Record, Void> schedule() {
        return new Callable1<Record, Void>() {
            public Void call(Record record) throws Exception {
                scheduler.schedule(record.get(JOB_ID), httpTask(parseRequest(record.get(REQUEST))), record.get(INTERVAL));
                return VOID;
            }
        };
    }
}
