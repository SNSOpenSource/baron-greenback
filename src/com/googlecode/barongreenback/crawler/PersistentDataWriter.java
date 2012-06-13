package com.googlecode.barongreenback.crawler;

import java.util.concurrent.ExecutorService;

public class PersistentDataWriter extends JobExecutor {
    public PersistentDataWriter(ExecutorService executor) {
        super(executor);
    }
}