package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.RecordMethods;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.HttpMessageParser.parseRequest;

public class HttpScheduler {
    public static final Keyword<String> SCHEDULED_REQUESTS = keyword("scheduledRequests", String.class);


    public static final Keyword<String> REQUEST = keyword("request", String.class);
    public static final Keyword<String> JOB_ID = keyword("scheduledRequests_id", String.class);

    public static final Keyword<Long> INITIAL_DELAY = keyword("initialDelay", Long.class);
    public static final Keyword<Long> INTERVAL = keyword("delay", Long.class);
    public static final Keyword<TimeUnit> TIME_UNIT = keyword("timeUnit", TimeUnit.class);

    private final Records records = new MemoryRecords();
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new HashMap<String, ScheduledFuture<?>>();

    private final ScheduledExecutorService executorService;
    private final Application application;

    public HttpScheduler(final ScheduledExecutorService executorService, final Application application) {
        this.executorService = executorService;
        this.application = application;
        records.define(SCHEDULED_REQUESTS, JOB_ID, REQUEST, INITIAL_DELAY, INTERVAL, TIME_UNIT);
    }

    public synchronized UUID schedule(Request request, Record schedulerSpec) {
        UUID id = UUID.randomUUID();
        records.add(SCHEDULED_REQUESTS, schedulerSpec.set(REQUEST, request.toString()).set(JOB_ID, id.toString()));

        return schedule(id, request, schedulerSpec.get(INITIAL_DELAY), schedulerSpec.get(INTERVAL), schedulerSpec.get(TIME_UNIT));
    }

    public synchronized UUID reschedule(UUID id, Record schedulerSpec) {
        records.put(SCHEDULED_REQUESTS, RecordMethods.update(using(JOB_ID), schedulerSpec.set(JOB_ID, id.toString())));

        cancelScheduledFuture(id);

        Record record = job(id).get();
        return schedule(id, parseRequest(record.get(REQUEST)), record.get(INITIAL_DELAY), record.get(INTERVAL), record.get(TIME_UNIT));
    }

    public synchronized void remove(UUID id) {
        cancelScheduledFuture(id);
        records.remove(SCHEDULED_REQUESTS, where(JOB_ID, is(id.toString())));
    }

    public Sequence<Record> jobs() {
        return records.get(SCHEDULED_REQUESTS);
    }

    public Option<Record> job(UUID id) {
        return records.get(SCHEDULED_REQUESTS).filter(where(JOB_ID, is(id.toString()))).headOption();
    }

    private UUID schedule(UUID id, Request request, Long initialDelay, Long interval, TimeUnit timeUnit) {
        ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(httpTask(request), initialDelay, interval, timeUnit);
        scheduledFutures.put(id.toString(), scheduledFuture);
        return id;
    }

    private void cancelScheduledFuture(UUID id) {
        if (scheduledFutures.containsKey(id.toString())) {
            scheduledFutures.get(id.toString()).cancel(true);
        }
    }

    public Runnable httpTask(final Request request) {
        return new Runnable() {
            public void run() {
                try {
                    application.handle(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
