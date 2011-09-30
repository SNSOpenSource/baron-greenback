package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.Scheduler;
import com.googlecode.barongreenback.jobs.Job;
import com.googlecode.utterlyidle.Response;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class CountDownScheduler implements Scheduler {
    private final Scheduler scheduler;
    private final CountDownLatch latch;

    public CountDownScheduler(Scheduler scheduler, CountDownLatch latch) {
        this.scheduler = scheduler;
        this.latch = latch;
    }

    public Job schedule(String id, Callable<Response> command, long delay) {
        return scheduler.schedule(id, decorate(command), delay);
    }

    public void cancel(String id) {
        scheduler.cancel(id);
    }

    private Callable<Response> decorate(final Callable<Response> command) {
        return new Callable<Response>() {
            public Response call() throws Exception {
                Response response = command.call();
                latch.countDown();
                return response;
            }
        };
    }
}
