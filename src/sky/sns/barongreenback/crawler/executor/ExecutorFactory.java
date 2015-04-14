package com.googlecode.barongreenback.crawler.executor;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;

public interface ExecutorFactory extends Closeable {
	public <R extends Runnable> ThreadPoolJobExecutor<R> executor(
            int threads, int capacity, String name, Class<? extends BlockingQueue> queueClass);

}