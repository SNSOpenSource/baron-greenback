package com.googlecode.barongreenback.jobs;

import java.util.concurrent.ScheduledFuture;

public class ScheduledJobAdapter implements ScheduledJob {
    private final ScheduledFuture<?> scheduledFuture;

    public ScheduledJobAdapter(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        scheduledFuture.cancel(mayInterruptIfRunning);
    }
}
