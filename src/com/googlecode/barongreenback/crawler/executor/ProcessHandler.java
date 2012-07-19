package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.executor.JobExecutor;

import java.util.concurrent.ExecutorService;

public class ProcessHandler extends JobExecutor {
    public ProcessHandler(ExecutorService executor) {
        super(executor);
    }
}
