package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.time.Clock;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.googlecode.totallylazy.Functions.function;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Seconds.between;
import static java.util.concurrent.TimeUnit.SECONDS;

public class FixedScheduler implements Scheduler, Closeable {
    private final Map<UUID, Cancellable> jobs = new HashMap<UUID, Cancellable>();
    private final ScheduledExecutorService service;
    private final Clock clock;

    public FixedScheduler(Clock clock) {
        this.clock = clock;
        this.service = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void schedule(UUID id, Callable<?> command, Option<Date> start, long numberOfSeconds) {
        cancel(id);
        Date now = clock.now();
        FutureJob job = new FutureJob(service.scheduleAtFixedRate(function(command), between(now, start.getOrElse(now)), numberOfSeconds, SECONDS));
        jobs.put(id, job);
    }

    public void cancel(UUID id) {
        Cancellable job = jobs.remove(id);
        if (job != null) {
            job.cancel();
        }
    }

    @Override
    public void close() throws IOException {
        while (!jobs.keySet().isEmpty()) {
            cancel(sequence(jobs.keySet()).first());
        }
    }
}
