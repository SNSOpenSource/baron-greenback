package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.Option;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolJobExecutor implements JobExecutor {
    private final ThreadPoolExecutor executor;
    private final String name;

    public ThreadPoolJobExecutor(ThreadPoolExecutor executor, String name) {
        this.executor = executor;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Option<Integer> activeThreads() {
        return Option.some(executor.getActiveCount());
    }

    @Override
    public int size() {
        return executor.getQueue().size();
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
    }
}
