package com.googlecode.barongreenback.jobs;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Sequences.sequence;

public class FixedScheduler implements Scheduler, Closeable {
    private final Map<UUID, Cancellable> jobs = new HashMap<UUID, Cancellable>();
    private final ScheduledExecutorService service;

    public FixedScheduler(ScheduledExecutorService service) {
        this.service = service;
    }

    public Cancellable schedule(UUID id, Callable<?> command, long numberOfSeconds) {
        cancel(id);
        FutureJob job = new FutureJob(service.scheduleWithFixedDelay(asRunnable(command), 0, numberOfSeconds, TimeUnit.SECONDS));
        jobs.put(id, job);
        return job;
    }

    private Runnable asRunnable(final Callable<?> command) {
        return new Runnable() {
            public void run() {
                try {
                    command.call();
                } catch (Exception e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        };
    }

    public void cancel(UUID id) {
        Cancellable job = jobs.remove(id);
        if (job != null) {
            job.cancel();
        }
    }

    @Override
    public void close() throws IOException {
        while(!jobs.keySet().isEmpty()) {
            cancel(sequence(jobs.keySet()).first());
        }
    }
}
