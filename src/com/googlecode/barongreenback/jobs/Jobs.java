package com.googlecode.barongreenback.jobs;

import com.googlecode.barongreenback.crawler.UniqueRecords;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordMethods;
import com.googlecode.lazyrecords.Records;

import java.util.UUID;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Using.using;

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
