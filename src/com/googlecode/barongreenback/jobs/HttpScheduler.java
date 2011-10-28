package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Date;
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
    public static final Keyword<Long> INTERVAL = keyword("interval", Long.class);
    public static final Keyword<Long> DURATION = keyword("duration", Long.class);
    public static final Keyword<Date> STARTED = keyword("started", Date.class);
    public static final Keyword<Date> COMPLETED = keyword("completed", Date.class);
    public static final Keyword<Boolean> RUNNING = keyword("runing", Boolean.class);

    private final Records records;
    private final Scheduler scheduler;
    private final Application application;
    private final Clock clock;

    public HttpScheduler(final Records records, final Scheduler scheduler, final Application application, final Clock clock) {
        this.records = records;
        this.scheduler = scheduler;
        this.application = application;
        this.clock = clock;
        define(records);
    }

    private static void define(Records records) {
        records.define(JOBS, JOB_ID, REQUEST, RESPONSE, INTERVAL, DURATION, STARTED, COMPLETED, RUNNING);
    }

    public UUID schedule(Record possiblePartialRecord) {
        records.put(JOBS, update(using(JOB_ID), possiblePartialRecord));
        UUID id = possiblePartialRecord.get(JOB_ID);

        Record fullRecord = job(id).get();
        scheduler.schedule(id, httpTask(id, application, parseRequest(fullRecord.get(REQUEST))), fullRecord.get(INTERVAL));
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

    private Callable<Void> httpTask(final UUID id, final Application application, final Request request) {
        return new Callable<Void>() {
            public Void call() throws Exception {
                final Date started = clock.now();
                application.usingRequestScope(updateJob(record().
                        set(JOB_ID, id).
                        set(RESPONSE, null).
                        set(STARTED, started).
                        set(COMPLETED, null).
                        set(RUNNING, true)));
                final Response response = application.handle(request);
                Date completed = clock.now();
                return application.usingRequestScope(updateJob(record().
                        set(JOB_ID, id).
                        set(RESPONSE, response.toString()).
                        set(DURATION, calculateSeconds(started, completed)).
                        set(COMPLETED, completed).
                        set(RUNNING, false)));
            }
        };
    }

    private Callable1<Container, Void> updateJob(final Record record) {
        return new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                Records newTransaction = container.get(Records.class);
                define(newTransaction);
                newTransaction.put(JOBS, update(using(JOB_ID), record));
                return VOID;
            }
        };
    }

    private static Long calculateSeconds(Date start, Date end) {
        return (end.getTime() - start.getTime()) / 1000L;
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
                scheduler.schedule(record.get(JOB_ID), httpTask(record.get(JOB_ID), application, parseRequest(record.get(REQUEST))), record.get(INTERVAL));
                return VOID;
            }
        };
    }
}
