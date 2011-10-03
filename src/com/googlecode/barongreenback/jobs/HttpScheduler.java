package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.UUID;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;

public class HttpScheduler {
    public static final Keyword<String> JOBS = keyword("jobs", String.class);

    public static final Keyword<UUID> JOB_ID = keyword("jobs_id", UUID.class);
    public static final Keyword<String> REQUEST = keyword("request", String.class);
    public static final Keyword<String> RESPONSE = keyword("response", String.class);
    public static final Keyword<Long> SECONDS = keyword("seconds", Long.class);

    private final Records records;
    private final Scheduler scheduler;
    private final Application application;

    public HttpScheduler(final Records records, final Scheduler scheduler, final Application application) {
        this.records = records;
        this.scheduler = scheduler;
        this.application = application;
        define(records);
    }

    private static void define(Records records) {
        records.define(JOBS, JOB_ID, REQUEST, RESPONSE, SECONDS);
    }

    public UUID schedule(Record possiblePartialRecord) {
        records.put(JOBS, update(using(JOB_ID), possiblePartialRecord));
        UUID id = possiblePartialRecord.get(JOB_ID);

        Record fullRecord = job(id).get();
        scheduler.schedule(id, httpTask(application, parseRequest(fullRecord.get(REQUEST))), fullRecord.get(SECONDS));
        return id;
    }

    public void start() {
        jobs().each(schedule());
    }

    public void stop() {
        jobs().each(cancel());
    }

    public void remove(UUID id) {
        scheduler.cancel(id);
        records.remove(JOBS, where(JOB_ID, is(id)));
    }

    public Sequence<Record> jobs() {
        return records.get(JOBS);
    }

    public Option<Record> job(UUID id) {
        return records.get(JOBS).find(where(JOB_ID, is(id)));
    }

    private static Callable<Void> httpTask(final Application application, final Request request) {
        return new Callable<Void>() {
            public Void call() throws Exception {
                final Response response = application.handle(request);
                return application.usingRequestScope(new Callable1<Container, Void>() {
                    public Void call(Container container) throws Exception {
                        Records records1 = container.get(Records.class);
                        define(records1);
                        records1.put(JOBS, update(using(JOB_ID), record().set(RESPONSE, response.toString())));
                        return VOID;
                    }
                });
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
                scheduler.schedule(record.get(JOB_ID), httpTask(application, parseRequest(record.get(REQUEST))), record.get(SECONDS));
                return VOID;
            }
        };
    }
}
