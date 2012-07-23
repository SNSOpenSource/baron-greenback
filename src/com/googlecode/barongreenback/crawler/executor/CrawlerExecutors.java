package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.crawler.BlockingRetryRejectedExecutionHandler;
import com.googlecode.totallylazy.Callable1;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CrawlerExecutors implements Closeable {
    private InputHandler inputHandler;
    private ProcessHandler processHandler;
    private OutputHandler outputHandler;
    private Integer inputHandlerThreads = 10;
    private Integer processHandlerThreads = 1;
    private Integer outputHandlerThreads = 1;
    private Integer inputHandlerCapacity = 0;
    private Integer processHandlerCapacity = 50;
    private Integer outputHandlerCapacity = 0;

    public Integer getInputHandlerThreads() {
        return inputHandlerThreads;
    }

    public Integer getProcessHandlerThreads() {
        return processHandlerThreads;
    }

    public Integer getOutputHandlerThreads() {
        return outputHandlerThreads;
    }

    public Integer getInputHandlerCapacity() {
        return inputHandlerCapacity;
    }

    public Integer getProcessHandlerCapacity() {
        return processHandlerCapacity;
    }

    public Integer getOutputHandlerCapacity() {
        return outputHandlerCapacity;
    }

    public CrawlerExecutors() {
        initialise();
    }

    public InputHandler inputHandler() {
        return inputHandler;
    }

    public ProcessHandler processHandler() {
        return processHandler;
    }

    public OutputHandler outputHandler() {
        return outputHandler;
    }

    public void setInputHandlerThreads(Integer inputHandlerThreads) {
        this.inputHandlerThreads = inputHandlerThreads;
        resetExecutors();
    }

    public void setProcessHandlerThreads(Integer processHandlerThreads) {
        this.processHandlerThreads = processHandlerThreads;
        resetExecutors();
    }

    public void setOutputHandlerThreads(Integer outputHandlerThreads) {
        this.outputHandlerThreads = outputHandlerThreads;
        resetExecutors();
    }

    public void setInputHandlerCapacity(Integer inputHandlerCapacity) {
        this.inputHandlerCapacity = inputHandlerCapacity;
        resetExecutors();
    }

    public void setProcessHandlerCapacity(Integer processHandlerCapacity) {
        this.processHandlerCapacity = processHandlerCapacity;
        resetExecutors();
    }

    public void setOutputHandlerCapacity(Integer outputHandlerCapacity) {
        this.outputHandlerCapacity = outputHandlerCapacity;
        resetExecutors();
    }

    private void initialise() {
        this.inputHandler = createHandler(inputHandlerThreads, inputHandlerCapacity);
        this.processHandler = createProcessHandler(processHandlerThreads, processHandlerCapacity);
        this.outputHandler = createOutputHandler(outputHandlerThreads, outputHandlerCapacity);
    }

    public void resetExecutors() {
        close();
        initialise();
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

    private OutputHandler createOutputHandler(int threads, int capacity) {
        return new OutputHandler(createExecutor(threads, new LinkedBlockingQueue<Runnable>()));
    }

    private ProcessHandler createProcessHandler(int threads, int capacity) {
        return new ProcessHandler(createExecutor(threads, new LinkedBlockingQueue<Runnable>(capacity)));
    }

    public InputHandler createHandler(int threads, int capacity) {
        return new InputHandler(createExecutor(threads, new LinkedBlockingQueue<Runnable>()));
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
