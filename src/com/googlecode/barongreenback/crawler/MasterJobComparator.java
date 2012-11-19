package com.googlecode.barongreenback.crawler;

import java.util.Comparator;

public class MasterJobComparator implements Comparator<HttpJob> {
    @Override
    public int compare(HttpJob jobA, HttpJob jobB) {
        return priorityFor(jobA) - priorityFor(jobB);
    }

    private int priorityFor(HttpJob aJob) {
        return aJob instanceof MasterPaginatedHttpJob ? 0 : 1;
    }
}
