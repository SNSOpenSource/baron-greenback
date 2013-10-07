package com.googlecode.barongreenback.crawler;

import java.util.Comparator;

import static com.googlecode.barongreenback.crawler.StagedJob.functions.createdDate;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.comparators.Comparators.comparators;

public class StagedJobComparator {
    private static int priorityFor(StagedJob aJob) {
        if (aJob instanceof MasterPaginatedHttpJob) return -1;
        if (aJob instanceof PaginatedHttpJob) return 1;
        return 0;
    }

    public static Comparator<StagedJob> masterJobsFirst() {
        return new Comparator<StagedJob>() {
            @Override
            public int compare(StagedJob jobA, StagedJob jobB) {
                return priorityFor(jobA) - priorityFor(jobB);
            }
        };
    }

    public static Comparator<StagedJob> newestJobsFirst() {
        return descending(createdDate());
    }

    public static Comparator<StagedJob> masterJobsThenNewest() {
        return comparators(newestJobsFirst(), masterJobsFirst());
    }
}
