package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.BlockingRetryRejectedExecutionHandler;
import com.googlecode.barongreenback.crawler.DataWriter;
import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.barongreenback.crawler.StatusMonitor;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Lazy;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.INPUT_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.INPUT_HANDLER_THREADS;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.OUTPUT_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.OUTPUT_HANDLER_THREADS;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.PROCESS_HANDLER_CAPACITY;
import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.PROCESS_HANDLER_THREADS;
import static com.googlecode.totallylazy.Callables.value;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Lazy.lazy;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class CrawlerExecutors implements Closeable {

    private ConcurrentHashMap<String, Lazy<JobExecutor>> inputHandler = new ConcurrentHashMap<String, Lazy<JobExecutor>>();
    private ConcurrentHashMap<Definition, Lazy<JobExecutor>> processHandler = new ConcurrentHashMap<Definition, Lazy<JobExecutor>>();
    private ConcurrentHashMap<Definition, Lazy<DataWriter>> outputHandler = new ConcurrentHashMap<Definition, Lazy<DataWriter>>();
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
    }

    private DataWriter outputExecutor(String suffix) {
        return createDataWriter(configValues.get(OUTPUT_HANDLER_THREADS), configValues.get(OUTPUT_HANDLER_CAPACITY), 1, "Writing " + suffix);
    }

    private ThreadPoolJobExecutor processExecutor(String suffix) {
        return jobExecutor(configValues.get(PROCESS_HANDLER_THREADS), configValues.get(PROCESS_HANDLER_CAPACITY), "Processing " + suffix);
    }

    private ThreadPoolJobExecutor inputExecutor(String suffix) {
        return jobExecutor(configValues.get(INPUT_HANDLER_THREADS), configValues.get(INPUT_HANDLER_CAPACITY), "Reading " + suffix);
    }

    public void resetExecutors() {
        close();
        inputHandler.clear();
        processHandler.clear();
        outputHandler.clear();
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

    public JobExecutor inputHandler(StagedJob job) {
        final String authority = job.datasource().uri().authority();
        inputHandler.putIfAbsent(authority, new Lazy<JobExecutor>(){
            @Override
            protected JobExecutor get() throws Exception {
                return inputExecutor(authority);
            }
        });
        return inputHandler.get(authority).value();
    }

    public JobExecutor processHandler(StagedJob job) {
        final Definition definition = job.destination();
        processHandler.putIfAbsent(definition, new Lazy<JobExecutor>(){
            @Override
            protected JobExecutor get() throws Exception {
                return processExecutor(definition.name());
            }
        });
        return processHandler.get(definition).value();
    }

    public DataWriter outputHandler(StagedJob job) {
        final Definition definition = job.destination();
        outputHandler.putIfAbsent(definition, new Lazy<DataWriter>(){
            @Override
            protected DataWriter get() throws Exception {
                return outputExecutor(definition.name());
            }
        });
        return outputHandler.get(definition).value();
    }

    public Sequence<JobExecutor> statusMonitors() {
        Sequence<JobExecutor> inputHandlers = sequence(inputHandler.values()).map(value(JobExecutor.class));
        Sequence<JobExecutor> processHandlers = sequence(processHandler.values()).map(value(JobExecutor.class));
        Sequence<DataWriter> outputHandlers = sequence(outputHandler.values()).map(value(DataWriter.class));

        return Sequences.flatten(sequence(inputHandlers, processHandlers, outputHandlers));
    }

    public void handlerValues(Sequence<Pair<CrawlerConfigValues, Integer>> executorValues) {
        configValues.putAll(Maps.map(executorValues));
        resetExecutors();
    }

    @Override
    public void close() {
        statusMonitors().each(safeClose());
    }

    private static ThreadPoolExecutor executor(int threads, int capacity, Class<? extends BlockingQueue> queueClass) {
        return new ThreadPoolExecutor(threads, threads == 0 ? Integer.MAX_VALUE : threads,
                60L, TimeUnit.SECONDS,
                queue(queueClass, capacity),
                new BlockingRetryRejectedExecutionHandler());
    }

    private static ThreadPoolJobExecutor jobExecutor(int threads, int capacity, String name) {
        return new ThreadPoolJobExecutor(executor(threads, capacity, PriorityBlockingQueue.class), name);
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

