package com.googlecode.barongreenback.jobs;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerAdapter implements FixedScheduler{
    private final ScheduledExecutorService service;

    public SchedulerAdapter(ScheduledExecutorService service) {
        this.service = service;
    }

    public ScheduledJob scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return new ScheduledJobAdapter(service.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }
}
