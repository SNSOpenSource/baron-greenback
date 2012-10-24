package com.googlecode.barongreenback.jobs;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface Scheduler {
    void schedule(UUID id, Callable<?> command, Date start, long numberOfSeconds);

    void schedule(UUID id, Callable<?> command, long numberOfSeconds);

    void cancel(UUID id);
}