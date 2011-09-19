package com.googlecode.barongreenback.jobs;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface FixedScheduler {
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
