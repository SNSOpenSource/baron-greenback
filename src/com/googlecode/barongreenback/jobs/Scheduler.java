package com.googlecode.barongreenback.jobs;

import java.util.UUID;
import java.util.concurrent.Callable;

public interface Scheduler {
    Cancellable schedule(UUID id, Callable<?> command, long numberOfSeconds);

    void cancel(UUID id);
}
