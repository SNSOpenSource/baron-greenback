package com.googlecode.barongreenback.jobs;

import java.util.concurrent.TimeUnit;

public interface FixedScheduler {
    ScheduledJob scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
