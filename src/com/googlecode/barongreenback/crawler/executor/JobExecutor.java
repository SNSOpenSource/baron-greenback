package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.StatusMonitor;
import com.googlecode.totallylazy.Option;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Some.some;

public class JobExecutor implements StatusMonitor {
    public final ExecutorService executor;
    private final String name;

    public JobExecutor(ExecutorService executor, String name) {
        this.executor = executor;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Option<Integer> activeThreads() {
        if (executor instanceof ThreadPoolExecutor) {
            return Option.some(((ThreadPoolExecutor) executor).getActiveCount());
        }

        return none();
    }

    @Override
    public int size() {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor)executor).getQueue().size();
        }

        return -1;
    }
}
