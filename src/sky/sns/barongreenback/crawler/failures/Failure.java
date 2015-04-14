package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.totallylazy.Eq;
import com.googlecode.totallylazy.annotations.multimethod;

import java.util.Date;

public class Failure extends Eq {
    private final Job job;
    private final String reason;
    private final Long duration;

    private Failure(Job job, String reason, Long duration) {
        this.job = job;
        this.reason = reason;
        this.duration = duration;
    }

    public static Failure failure(Job job, String reason, Long duration) {
        return new Failure(job, reason, duration);
    }

    public Job job() {
        return job;
    }

    public String reason() {
        return reason;
    }

    public Date date() {
        return job.createdDate();
    }

    public Long duration() {
        return duration;
    }

    @Override
    public int hashCode() {
        return job.hashCode() * reason.hashCode() * duration.hashCode();
    }

    @multimethod
    public boolean equals(Failure other) {
        return (other.duration == duration) && other.job.equals(job) && other.reason.equals(reason);
    }

    @Override
    public String toString() {
        return String.format("job: %s, reason: %s, date: %s, duration: %d", job.toString(), reason, date(), duration);
    }
}
