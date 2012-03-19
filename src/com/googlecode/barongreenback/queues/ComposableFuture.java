package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ComposableFuture<T> extends FutureTask<T> {
    private Callable1<T, ?> completed;

    private ComposableFuture(Callable<T> task, Callable1<T, ?> completed) {
        super(task);
        this.completed = completed;
    }

    @Override
    protected void done() {
        try {
            completed.call(get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> ComposableFuture<T> compose(Callable<T> task, Callable1<T, ?> completed) {
        return new ComposableFuture<T>(task, completed);
    }
}
