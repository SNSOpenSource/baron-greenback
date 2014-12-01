package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.concurrent.NamedExecutors;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Unchecked.cast;

public class ThreadPoolExecutorFactory implements ExecutorFactory {
    private final CloseableList executors = new CloseableList();

    @Override
    public <R extends Runnable> ThreadPoolJobExecutor<R> executor(int threads, int capacity, String name, Class<? extends BlockingQueue> queueClass) {
        return executors.manage(new ThreadPoolJobExecutor<R>(executor(threads, capacity, queueClass, name), name));
    }

    private ThreadPoolExecutor executor(int threads, int capacity, Class<? extends BlockingQueue> queueClass, String name) {
        return new ThreadPoolExecutor(threads, threads == 0 ? Integer.MAX_VALUE : threads,
                60L, TimeUnit.SECONDS,
                queue(queueClass, capacity),
                NamedExecutors.namedThreadFactory(name),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private BlockingQueue<Runnable> queue(Class<? extends BlockingQueue> queueClass, int capacity) {
        Container container = Containers.container().add(BlockingQueue.class, queueClass);
        if (capacity > 0) {
            container.addInstance(int.class, capacity);
        }
        return cast(container.get(BlockingQueue.class));
    }

    @Override
    public void close() throws IOException {
        executors.close();
    }
}
