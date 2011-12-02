package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;

import java.util.UUID;

import static com.googlecode.totallylazy.records.Keywords.keyword;

public class Job {
    public static final Keyword<UUID> JOB_ID = keyword("jobs_id", UUID.class);
    public static final Keyword<String> REQUEST = keyword("request", String.class);
    public static final Keyword<Long> INTERVAL = keyword("interval", Long.class);

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

    public Record asRecord() {
        return record;
    }
}
