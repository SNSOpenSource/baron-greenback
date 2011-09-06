package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.RecordMethods;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.Using;
import com.googlecode.totallylazy.records.memory.MemoryRecords;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Using.using;

public class HttpScheduler {
    public static final Keyword<String> SCHEDULED_REQUESTS = keyword("scheduledRequests", String.class);

    public static final Keyword<String> REQUEST = keyword("request", String.class);

    public static final Keyword<Long> INITIAL_DELAY = keyword("initialDelay", Long.class);
    public static final Keyword<Long> DELAY = keyword("delay", Long.class);
    public static final Keyword<TimeUnit> TIME_UNIT = keyword("timeUnit", TimeUnit.class);

    private final Records records = new MemoryRecords();
    private final Map<Request, ScheduledFuture<?>> scheduledFutures = new HashMap<Request, ScheduledFuture<?>>();

    private final ScheduledExecutorService executorService;

    public HttpScheduler(ScheduledExecutorService executorService) {
        this.executorService = executorService;
        records.define(SCHEDULED_REQUESTS, REQUEST, INITIAL_DELAY, DELAY, TIME_UNIT);
    }

    public synchronized void schedule(Request request, Record schedulerSpec) {
        records.put(SCHEDULED_REQUESTS, RecordMethods.update(using(REQUEST), schedulerSpec.set(REQUEST, request.toString())));

        if(scheduledFutures.containsKey(request)) {
            scheduledFutures.get(request).cancel(true);
        }
        ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(httpTask(request), schedulerSpec.get(INITIAL_DELAY), schedulerSpec.get(DELAY), schedulerSpec.get(TIME_UNIT));
        scheduledFutures.put(request, scheduledFuture);
    }

    private Runnable httpTask(final Request request) {
        return new Runnable() {
            public void run() {
                try {
                    new ClientHttpHandler().handle(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public Sequence<Record> jobs() {
        return records.get(SCHEDULED_REQUESTS);
    }
}
