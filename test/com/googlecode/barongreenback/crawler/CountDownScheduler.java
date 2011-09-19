package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.FixedScheduler;
import com.googlecode.barongreenback.jobs.ScheduledJob;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownScheduler implements FixedScheduler{
    private final FixedScheduler fixedScheduler;
    private final CountDownLatch latch;

    public CountDownScheduler(FixedScheduler fixedScheduler, CountDownLatch latch) {
        this.fixedScheduler = fixedScheduler;
        this.latch = latch;
    }

    public ScheduledJob scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return fixedScheduler.scheduleWithFixedDelay(decorate(command), initialDelay, delay, unit);
    }

    private Runnable decorate(final Runnable command) {
        return new Runnable() {
            public void run() {
                command.run();
                latch.countDown();
            }
        };
    }
}
