package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.googlecode.barongreenback.queues.ComposableFuture.compose;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class CpuBoundedCompleter implements Completer {
    private final ExecutorService executors = newFixedThreadPool(getRuntime().availableProcessors() - 1);

    @Override
    public <T> void complete(Callable<T> task, Callable1<T, ?> completion) {
        executors.execute(compose(task, completion));
    }
}
