package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.concurrent.NamedExecutors;
import com.googlecode.utterlyidle.services.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static com.googlecode.totallylazy.Functions.function;

public class CpuBoundedCompleter implements Completer, Service, Closeable {
    private volatile ExecutorService executor;

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
        if (executor == null) executor = NamedExecutors.newCpuThreadPool(CpuBoundedCompleter.class);
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
