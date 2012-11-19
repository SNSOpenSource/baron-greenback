package com.googlecode.barongreenback.crawler;

import java.util.Comparator;

import static com.googlecode.barongreenback.crawler.StagedJob.functions.createdDate;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.comparators.Comparators.comparators;

public class StagedJobComparator{
    private static int priorityFor(StagedJob aJob) {
        return aJob instanceof MasterPaginatedHttpJob ? 0 : 1;
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
        return comparators(masterJobsFirst(), newestJobsFirst());
    }
}
