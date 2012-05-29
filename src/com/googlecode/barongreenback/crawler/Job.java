package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;

public class Job {
    private final HttpDataSource dataSource;
    private final Definition destination;

    private Job(HttpDataSource dataSource, Definition destination) {
        this.dataSource = dataSource;
        this.destination = destination;
    }

    public static Job job(HttpDataSource dataSource, Definition destination) {
        return new Job(dataSource, destination);
    }

    public HttpDataSource dataSource() {
        return dataSource;
    }

    public Definition destination() {
        return destination;
    }
}
