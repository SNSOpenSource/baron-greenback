package com.googlecode.barongreenback.crawler;

import java.util.concurrent.ExecutorService;

public class JobExecutor<T> {
    public final ExecutorService executor;

    public JobExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}
