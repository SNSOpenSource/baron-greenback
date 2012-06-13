package com.googlecode.barongreenback.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class JobExecutor implements StatusMonitor {
    public final ExecutorService executor;

    public JobExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int size() {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)executor).getQueue().size();
        }

        return -1;
    }
}
