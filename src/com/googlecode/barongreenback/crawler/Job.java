package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;

public class Job {
    private final DataSource dataSource;
    private final Definition destination;

    private Job(DataSource dataSource, Definition destination) {
        this.dataSource = dataSource;
        this.destination = destination;
    }

    public static Job job(DataSource dataSource, Definition destination) {
        return new Job(dataSource, destination);
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public Definition destination() {
        return destination;
    }
}
