package com.googlecode.barongreenback.jobs;

import com.googlecode.barongreenback.crawler.UniqueRecords;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.RecordMethods;
import com.googlecode.totallylazy.records.Records;

import java.util.UUID;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Using.using;

public class Jobs {
    public static final Keyword<String> JOBS = keyword("jobs", String.class);
    private final Records records;

    public Jobs(final Records records) {
        this.records = records;
        define(records);
    }

    private static void define(Records records) {
        records.define(JOBS, Job.JOB_ID, Job.REQUEST, Job.RESPONSE, Job.INTERVAL, Job.DURATION, Job.STARTED, Job.COMPLETED, Job.RUNNING);
    }

    public void createOrUpdate(Job job) {
        records.put(Jobs.JOBS, RecordMethods.update(using(Job.JOB_ID), job.toRecord()));
    }

    public Sequence<Record> jobs() {
        return records.get(Jobs.JOBS).realise().filter(where(Job.REQUEST, not(empty()))).filter(new UniqueRecords(Job.JOB_ID));
    }

    public void remove(UUID id) {
        records.remove(Jobs.JOBS, where(Job.JOB_ID, is(id)));
    }
}
