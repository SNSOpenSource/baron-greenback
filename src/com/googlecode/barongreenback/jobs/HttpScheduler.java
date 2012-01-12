package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.rendering.ExceptionRenderer;
import com.googlecode.yadic.Container;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.jobs.Job.INTERVAL;
import static com.googlecode.barongreenback.jobs.Job.JOB_ID;
import static com.googlecode.barongreenback.jobs.Job.REQUEST;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;
import static com.googlecode.utterlyidle.Responses.response;

public class HttpScheduler {
    private final Jobs jobs;
    private final Scheduler scheduler;
    private final Application application;
    private final Clock clock;

    public HttpScheduler(final Jobs jobs, final Scheduler scheduler, final Application application, final Clock clock) {
        this.jobs = jobs;
        this.scheduler = scheduler;
        this.application = application;
        this.clock = clock;
    }

    public UUID schedule(Job job) {
        jobs.createOrUpdate(job);

        UUID id = job.get(JOB_ID);
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
        jobs.remove(id);
    }

    public Sequence<Record> jobs() {
        return jobs.jobs();
    }

    public Option<Record> job(UUID id) {
        return jobs().find(where(JOB_ID, is(id)));
    }

    private Callable<Void> httpTask(final UUID id, final Application application, final Request request) {
        return new Callable<Void>() {
            public Void call() throws Exception {
                final Date started = clock.now();
                try {
                    application.usingRequestScope(updateJob(
                            Job.job(id).response(null).started(started).completed(null).running(true)));
                    final Response response = application.handle(request);
                    Date completed = clock.now();
                    return application.usingRequestScope(updateJob(
                            Job.job(id).response(response.toString()).duration(calculateSeconds(started, completed)).completed(completed).running(false)));
                } catch (Exception e) {
                    Date completed = clock.now();
                    return application.usingRequestScope(updateJob(
                            Job.job(id).response(response(Status.INTERNAL_SERVER_ERROR).
                                    bytes(ExceptionRenderer.toString(e).getBytes()).toString()).
                                    duration(calculateSeconds(started, completed)).
                                    completed(completed).running(false)));
                }
            }
        };
    }

    private Callable1<Container, Void> updateJob(final Job job) {
        return new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                Jobs newTransaction = container.get(Jobs.class);
                newTransaction.createOrUpdate(job);
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
