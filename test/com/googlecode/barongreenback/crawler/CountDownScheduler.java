package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.Scheduler;
import com.googlecode.barongreenback.jobs.Job;
import com.googlecode.totallylazy.Runnables;

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

    public Job schedule(UUID id, Callable<?> command, long numberOfSeconds) {
        return scheduler.schedule(id, decorate(command), numberOfSeconds);
    }

    public void cancel(UUID id) {
        scheduler.cancel(id);
    }

    private Callable<Void> decorate(final Callable<?> command) {
        return new Callable<Void>() {
            public Void call() throws Exception {
                command.call();
                latch.countDown();
                return Runnables.VOID;
            }
        };
    }
}
