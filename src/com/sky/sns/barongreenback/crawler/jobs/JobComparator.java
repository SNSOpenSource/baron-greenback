package com.sky.sns.barongreenback.crawler.jobs;

import com.sky.sns.barongreenback.crawler.jobs.Job;
import com.sky.sns.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import com.sky.sns.barongreenback.crawler.jobs.PaginatedHttpJob;

import java.util.Comparator;

import static com.sky.sns.barongreenback.crawler.jobs.Job.functions.createdDate;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.comparators.Comparators.comparators;

public class JobComparator {
    private static int priorityFor(Job aJob) {
        if (aJob instanceof MasterPaginatedHttpJob) return -1;
        if (aJob instanceof PaginatedHttpJob) return 1;
        return 0;
    }

    public static Comparator<Job> masterJobsFirst() {
        return new Comparator<Job>() {
            @Override
            public int compare(Job jobA, Job jobB) {
                return priorityFor(jobA) - priorityFor(jobB);
            }
        };
    }

    public static Comparator<Job> newestJobsFirst() {
        return descending(createdDate());
    }

    public static Comparator<Job> masterJobsThenNewest() {
        return comparators(newestJobsFirst(), masterJobsFirst());
    }
}
