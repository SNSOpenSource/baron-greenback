package com.googlecode.barongreenback.crawler.executor;

import static com.googlecode.totallylazy.Unchecked.cast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.googlecode.barongreenback.crawler.BlockingRetryRejectedExecutionHandler;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;

public class ThreadPoolExecutorFactory implements ExecutorFactory {
	
    private ThreadPoolExecutor executor(int threads, int capacity, Class<? extends BlockingQueue> queueClass) {
        return new ThreadPoolExecutor(threads, threads == 0 ? Integer.MAX_VALUE : threads,
                60L, TimeUnit.SECONDS,
                queue(queueClass, capacity),
                new BlockingRetryRejectedExecutionHandler());
    }

    @Override
    public ThreadPoolJobExecutor jobExecutor(int threads, int capacity, String name) {
        return new ThreadPoolJobExecutor(executor(threads, capacity, PriorityBlockingQueue.class), name);
    }

    private BlockingQueue<Runnable> queue(Class<? extends BlockingQueue> queueClass, int capacity) {
        Container container = Containers.container().add(BlockingQueue.class, queueClass);
        if (capacity > 0) {
            container.addInstance(int.class, capacity);
        }
        return cast(container.get(BlockingQueue.class));
    }

}
