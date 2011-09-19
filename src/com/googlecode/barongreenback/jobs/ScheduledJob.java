package com.googlecode.barongreenback.jobs;

public interface ScheduledJob {
    void cancel(boolean mayInterruptIfRunning);
}
