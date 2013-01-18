package com.googlecode.barongreenback.crawler.executor;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
    private final ThreadFactory threadFactory;
    private final String name;

    public NamedThreadFactory(ThreadFactory threadFactory, String name) {
        this.threadFactory = threadFactory;
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = threadFactory.newThread(runnable);
        thread.setName(String.format("%s: %s", name, thread.getName()));
        return thread;
    }
}
