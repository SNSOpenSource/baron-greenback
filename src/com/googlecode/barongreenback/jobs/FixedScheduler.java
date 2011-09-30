package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.rendering.ExceptionRenderer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.lucene.DirectoryActivator.DEFAULT_DIRECTORY;
import static com.googlecode.totallylazy.Files.file;

public class FixedScheduler implements Scheduler {
    private final Map<String, Job> jobs = new HashMap<String, Job>();
    private final ScheduledExecutorService service;

    public FixedScheduler(ScheduledExecutorService service) {
        this.service = service;
    }

    public Job schedule(String id, Callable<Response> command, long delay) {
        cancel(id);
        FutureJob job = new FutureJob(service.scheduleWithFixedDelay(captureResponse(id, command), 0, delay, TimeUnit.SECONDS));
        jobs.put(id, job);
        return job;
    }

    private Runnable captureResponse(final String id, final Callable<Response> callable) {
        return new Runnable() {
            public void run() {
                writeResult(id, callable);
            }
        };
    }

    private void writeResult(String id, Callable<Response> callable) {
        try {
            Files.write(result(callable).getBytes("UTF-8"), fileFor(id));
        } catch (UnsupportedEncodingException e) {
            throw new LazyException(e);
        }
    }

    private File fileFor(String id) {
        return file(DEFAULT_DIRECTORY, String.format("job.%s.response", id));
    }

    private String result(Callable<Response> callable) {
        try {
            return callable.call().toString();
        } catch (Exception e) {
            return ExceptionRenderer.toString(e);
        }
    }

    public void cancel(String id) {
        Job job = jobs.remove(id);
        if (job != null) {
            job.cancel();
        }
    }
}
