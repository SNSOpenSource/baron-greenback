package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.StatusMonitor;

import java.io.Closeable;

public interface JobExecutor<R extends Runnable> extends StatusMonitor, Closeable {

    public void execute(R command);

}
