package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.crawler.executor.ExecutorFactory;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.totallylazy.Value;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class ViewsExecutor implements Value<JobExecutor<Runnable>>, Executor, Closeable {
    private final JobExecutor<Runnable> executor;

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

    @Override
    public void close() throws IOException {
        executor.close();
    }
}
