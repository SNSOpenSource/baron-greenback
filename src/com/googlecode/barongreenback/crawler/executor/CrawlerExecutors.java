package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.BlockingRetryRejectedExecutionHandler;
import com.googlecode.barongreenback.crawler.DataWriter;
import com.googlecode.barongreenback.crawler.StatusMonitor;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.INPUT_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.INPUT_HANDLER_THREADS;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.OUTPUT_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.OUTPUT_HANDLER_THREADS;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.PROCESS_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.PROCESS_HANDLER_THREADS;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class CrawlerExecutors implements Closeable {
    private JobExecutor inputHandler;
    private JobExecutor processHandler;
    private DataWriter outputHandler;
    private Map<CrawlerConfigValues, Integer> configValues = new HashMap<CrawlerConfigValues, Integer>();
    private final Application application;

    public CrawlerExecutors(Integer inputHandlerThreads, Integer inputHandlerCapacity, Integer processHandlerThreads, Integer processHandlerCapacity,
                            Integer outputHandlerThreads, Integer outputHandlerCapacity, Application application) {
        this.application = application;
        configValues.put(INPUT_HANDLER_THREADS, inputHandlerThreads);
        configValues.put(INPUT_HANDLER_CAPACITY, inputHandlerCapacity);
        configValues.put(PROCESS_HANDLER_THREADS, processHandlerThreads);
        configValues.put(PROCESS_HANDLER_CAPACITY, processHandlerCapacity);
        configValues.put(OUTPUT_HANDLER_THREADS, outputHandlerThreads);
        configValues.put(OUTPUT_HANDLER_CAPACITY, outputHandlerCapacity);
        initialise();
    }

    private void initialise() {
        this.inputHandler = jobExecutor(configValues.get(INPUT_HANDLER_THREADS), configValues.get(INPUT_HANDLER_CAPACITY), "Incoming");
        this.processHandler = jobExecutor(configValues.get(PROCESS_HANDLER_THREADS), configValues.get(PROCESS_HANDLER_CAPACITY), "Processing");
        this.outputHandler = createDataWriter(configValues.get(OUTPUT_HANDLER_THREADS), configValues.get(OUTPUT_HANDLER_CAPACITY), 1, "Writing");
    }

    public void resetExecutors() {
        close();
        initialise();
    }

    public Integer inputHandlerThreads() {
        return configValues.get(INPUT_HANDLER_THREADS);
    }

    public Integer processHandlerThreads() {
        return configValues.get(PROCESS_HANDLER_THREADS);
    }

    public Integer outputHandlerThreads() {
        return configValues.get(OUTPUT_HANDLER_THREADS);
    }

    public Integer inputHandlerCapacity() {
        return configValues.get(INPUT_HANDLER_CAPACITY);
    }

    public Integer processHandlerCapacity() {
        return configValues.get(PROCESS_HANDLER_CAPACITY);
    }

    public Integer outputHandlerCapacity() {
        return configValues.get(OUTPUT_HANDLER_CAPACITY);
    }

    public JobExecutor inputHandler() {
        return inputHandler;
    }

    public JobExecutor processHandler() {
        return processHandler;
    }

    public DataWriter outputHandler() {
        return outputHandler;
    }

    public Sequence<StatusMonitor> statusMonitors() {
        return Sequences.<StatusMonitor>sequence(inputHandler(), processHandler(), outputHandler());
    }

    public void handlerValues(Sequence<Pair<CrawlerConfigValues, Integer>> executorValues) {
        configValues.putAll(Maps.map(executorValues));
        resetExecutors();
    }

    @Override
    public void close() {
        sequence(inputHandler, processHandler, outputHandler).each(safeClose());
    }

    private static ThreadPoolExecutor executor(int threads, int capacity, Class<? extends BlockingQueue> queueClass) {
        return new ThreadPoolExecutor(threads, threads == 0 ? Integer.MAX_VALUE : threads,
                60L, TimeUnit.SECONDS,
                queue(queueClass, capacity),
                new BlockingRetryRejectedExecutionHandler());
    }

    private static ThreadPoolJobExecutor jobExecutor(int threads, int capacity, String name) {
        return new ThreadPoolJobExecutor(executor(threads, capacity, LinkedBlockingQueue.class), name);
    }

    private DataWriter createDataWriter(int threads, int capacity, int seconds, String name) {
        return new DataWriter(application, threads, seconds, name, capacity);
    }

    private static BlockingQueue<Runnable> queue(Class<? extends BlockingQueue> queueClass, int capacity) {
        Container container = Containers.container().add(BlockingQueue.class, queueClass);
        if (capacity > 0) {
            container.addInstance(int.class, capacity);
        }
        return cast(container.get(BlockingQueue.class));
    }
}

