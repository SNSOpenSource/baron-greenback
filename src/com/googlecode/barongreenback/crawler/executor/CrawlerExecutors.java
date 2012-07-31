package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.BlockingRetryRejectedExecutionHandler;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.crawler.executor.CrawlerConfigValues.*;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CrawlerExecutors implements Closeable {

    private JobExecutor inputHandler;
    private JobExecutor processHandler;
    private JobExecutor outputHandler;
    private Map<CrawlerConfigValues, Integer> configValues = new HashMap<CrawlerConfigValues, Integer>();

    public CrawlerExecutors(Integer inputHandlerThreads, Integer inputHandlerCapacity, Integer processHandlerThreads, Integer processHandlerCapacity, Integer outputHandlerThreads, Integer outputHandlerCapacity) {
        configValues.put(INPUT_HANDLER_THREADS, inputHandlerThreads);
        configValues.put(INPUT_HANDLER_CAPACITY, inputHandlerCapacity);
        configValues.put(PROCESS_HANDLER_THREADS, processHandlerThreads);
        configValues.put(PROCESS_HANDLER_CAPACITY, processHandlerCapacity);
        configValues.put(OUTPUT_HANDLER_THREADS, outputHandlerThreads);
        configValues.put(OUTPUT_HANDLER_CAPACITY, outputHandlerCapacity);
        initialise();
    }

    private void initialise() {
        this.inputHandler = createHandler(configValues.get(INPUT_HANDLER_THREADS), configValues.get(INPUT_HANDLER_CAPACITY), "Incoming");
        this.processHandler = createHandler(configValues.get(PROCESS_HANDLER_THREADS), configValues.get(PROCESS_HANDLER_CAPACITY), "Processing");
        this.outputHandler = createHandler(configValues.get(OUTPUT_HANDLER_THREADS), configValues.get(OUTPUT_HANDLER_CAPACITY), "Writing");
    }

    public void resetExecutors() {
        close();
        initialise();
    }

    public Integer getInputHandlerThreads() {
        return configValues.get(INPUT_HANDLER_THREADS);
    }

    public Integer getProcessHandlerThreads() {
        return configValues.get(PROCESS_HANDLER_THREADS);
    }

    public Integer getOutputHandlerThreads() {
        return configValues.get(OUTPUT_HANDLER_THREADS);
    }

    public Integer getInputHandlerCapacity() {
        return configValues.get(INPUT_HANDLER_CAPACITY);
    }

    public Integer getProcessHandlerCapacity() {
        return configValues.get(PROCESS_HANDLER_CAPACITY);
    }

    public Integer getOutputHandlerCapacity() {
        return configValues.get(OUTPUT_HANDLER_CAPACITY);
    }

    public JobExecutor inputHandler() {
        return inputHandler;
    }

    public JobExecutor processHandler() {
        return processHandler;
    }

    public JobExecutor outputHandler() {
        return outputHandler;
    }

    public void handlerValues(Sequence<Pair<CrawlerConfigValues, Integer>> executorValues) {
        configValues.putAll(Maps.map(executorValues));
        resetExecutors();
    }

    @Override
    public void close() {
        sequence(inputHandler, processHandler, outputHandler).map(executor()).forEach(shutdownNow());
    }

    public static ThreadPoolExecutor createExecutor(int threads, LinkedBlockingQueue<Runnable> workQueue) {
        return new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                workQueue,
                new BlockingRetryRejectedExecutionHandler());
    }

    public JobExecutor createHandler(int threads, int capacity, String name) {
        return new JobExecutor(createExecutor(threads, queueFor(capacity)), name);
    }

    private LinkedBlockingQueue<Runnable> queueFor(int capacity) {
        if (capacity > 0) {
            return new LinkedBlockingQueue<Runnable>(capacity);
        }
        return new LinkedBlockingQueue<Runnable>();
    }

    private Callable1<ExecutorService, List<Runnable>> shutdownNow() {
        return new Callable1<ExecutorService, List<Runnable>>() {
            @Override
            public List<Runnable> call(ExecutorService executor) throws Exception {
                return executor.shutdownNow();
            }
        };
    }

    private Callable1<JobExecutor, ExecutorService> executor() {
        return new Callable1<JobExecutor, ExecutorService>() {
            @Override
            public ExecutorService call(JobExecutor jobExecutor) throws Exception {
                return jobExecutor.executor;
            }
        };
    }
}

