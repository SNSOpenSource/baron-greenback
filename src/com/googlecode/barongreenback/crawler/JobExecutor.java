package com.googlecode.barongreenback.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class JobExecutor {
    public final ExecutorService executor;

    public JobExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    public int size() {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)executor).getQueue().size();
        }

        return -1;
    }
}
