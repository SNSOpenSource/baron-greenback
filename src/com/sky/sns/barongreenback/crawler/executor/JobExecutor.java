package com.sky.sns.barongreenback.crawler.executor;

import com.sky.sns.barongreenback.crawler.StatusMonitor;

import java.io.Closeable;

public interface JobExecutor<R extends Runnable> extends StatusMonitor, Closeable {

    public void execute(R command);

}
