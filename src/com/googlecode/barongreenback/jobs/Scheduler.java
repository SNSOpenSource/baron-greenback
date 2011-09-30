package com.googlecode.barongreenback.jobs;

import com.googlecode.utterlyidle.Response;

import java.util.concurrent.Callable;

public interface Scheduler {
    Job schedule(String id, Callable<Response> command, long delay);

    void cancel(String id);
}
