package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.crawler.executor.ExecutorFactory;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.totallylazy.Value;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class ViewsExecutor implements Value<JobExecutor<Runnable>>, Executor {
    private final JobExecutor<Runnable> executor;

    public ViewsExecutor(JobExecutor<Runnable> executor) {
        this.executor = executor;
    }

    public ViewsExecutor(ExecutorFactory factory) {
        this.executor = factory.executor(500, 0, "Views", LinkedBlockingQueue.class);
    }

    @Override
    public JobExecutor<Runnable> value() {
        return executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
