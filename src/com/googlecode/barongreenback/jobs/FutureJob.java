package com.googlecode.barongreenback.jobs;

import java.util.concurrent.Future;

public class FutureJob implements Cancellable {
    private final Future<?> future;

    public FutureJob(Future<?> future) {
        this.future = future;
    }

    public void cancel() {
        future.cancel(true);
    }
}
