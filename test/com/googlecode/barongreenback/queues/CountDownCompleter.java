package com.googlecode.barongreenback.queues;

import com.googlecode.totallylazy.Callable1;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class CountDownCompleter implements Completer {
    private final Completer delegate;
    private final CountDownLatch latch;

    public CountDownCompleter(Completer delegate, CountDownLatch latch) {
        this.delegate = delegate;
        this.latch = latch;
    }

    @Override
    public <T> void complete(Callable<T> task, Callable1<T, ?> completion) {
        delegate.complete(task, decorate(completion));
    }

    private <T, R> Callable1<T, R> decorate(final Callable1<T, R> callable) {
        return new Callable1<T, R>() {
            @Override
            public R call(T t) throws Exception {
                R result = callable.call(t);
                latch.countDown();
                return result;
            }
        };
    }

}
