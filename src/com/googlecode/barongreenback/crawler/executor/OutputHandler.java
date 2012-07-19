package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.executor.JobExecutor;

import java.util.concurrent.ExecutorService;

public class OutputHandler extends JobExecutor {
    public OutputHandler(ExecutorService executor) {
        super(executor);
    }
}