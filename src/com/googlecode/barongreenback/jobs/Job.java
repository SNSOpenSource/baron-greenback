package com.googlecode.barongreenback.jobs;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.MapRecord;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;

public class Job {
    public static final Keyword<UUID> JOB_ID = keyword("jobs_id", UUID.class);
    public static final Keyword<String> REQUEST = keyword("request", String.class);
    public static final Keyword<String> RESPONSE = keyword("response", String.class);
    public static final Keyword<Long> INTERVAL = keyword("interval", Long.class);
    public static final Keyword<Long> DURATION = keyword("duration", Long.class);
    public static final Keyword<Date> STARTED = keyword("started", Date.class);
    public static final Keyword<Date> COMPLETED = keyword("completed", Date.class);
    public static final Keyword<Boolean> RUNNING = keyword("runing", Boolean.class);

    private final Record record;

    public Job(Record record) {
        this.record = record;
    }

    public static Job job(UUID id) {
        return new Job(MapRecord.record().set(JOB_ID, id));
    }

    public Job interval(Long interval) {
        record.set(INTERVAL, interval);
        return this;
    }

    public Job request(String request) {
        record.set(REQUEST, request);
        return this;
    }

    public Job response(String response) {
        record.set(RESPONSE, response);
        return this;
    }

    public Job started(Date started) {
        record.set(STARTED, started);
        return this;
    }

    public Job completed(Date completed) {
        record.set(COMPLETED, completed);
        return this;
    }

    public Job running(boolean running) {
        record.set(RUNNING, running);
        return this;
    }

    public Job duration(Long duration) {
        record.set(DURATION, duration);
        return this;
    }

    public Record toRecord() {
        return record;
    }

    public <T> T get(Keyword<T> keyword) {
        return record.get(keyword);
    }

    public static Callable1<Record, UUID> asJobId() {
        return new Callable1<Record, UUID>() {
            public UUID call(Record record) throws Exception {
                return record.get(JOB_ID);
            }
        };
    }
}
