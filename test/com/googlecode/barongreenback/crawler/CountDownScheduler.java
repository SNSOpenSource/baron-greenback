package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.Scheduler;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class CountDownScheduler implements Scheduler {
    private final Scheduler scheduler;
    private final CountDownLatch latch;

    public CountDownScheduler(Scheduler scheduler, CountDownLatch latch) {
        this.scheduler = scheduler;
        this.latch = latch;
    }

    public void schedule(UUID id, Callable<?> command, long numberOfSeconds) {
        scheduler.schedule(id, decorate(latch, command), numberOfSeconds);
    }

    public void cancel(UUID id) {
        scheduler.cancel(id);
    }

    public static <T> Callable<T> decorate(final CountDownLatch latch, final Callable<T> command) {
        return new Callable<T>() {
            public T call() throws Exception {
                T result = command.call();
                latch.countDown();
                return result;
            }
        };
    }
}
