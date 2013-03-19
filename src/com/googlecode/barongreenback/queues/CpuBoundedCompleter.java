package com.googlecode.barongreenback.queues;

import com.googlecode.utterlyidle.services.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static com.googlecode.totallylazy.Functions.function;
import static com.googlecode.totallylazy.concurrent.NamedExecutors.newFixedThreadPool;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;

public class CpuBoundedCompleter implements Completer, Service, Closeable {
    private volatile ExecutorService executor;

    public static ExecutorService cpuBoundExecutorService() {
        return newFixedThreadPool(max(1, getRuntime().availableProcessors()), CpuBoundedCompleter.class);
    }

    @Override
    public void complete(Callable<?> task) {
        executors().execute(function(task));
    }

    private synchronized Executor executors() {
        if (executor == null) start();
        return executor;
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public synchronized void start() {
        if(executor == null) executor = cpuBoundExecutorService();
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
