package com.googlecode.barongreenback.crawler;

import java.io.Closeable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CrawlerExecutors implements Closeable {
    public InputHandler inputHandler() {
        return inputHandler;
    }

    public ProcessHandler processHandler() {
        return processHandler;
    }

    public OutputHandler outputHandler() {
        return outputHandler;
    }

    private InputHandler inputHandler;
    private ProcessHandler processHandler;
    private OutputHandler outputHandler;

    public CrawlerExecutors() {
        initialise();
    }

    private void initialise() {
        this.inputHandler = createInputHandler();
        this.processHandler = createProcessHandler();
        this.outputHandler = createOutputHandler();
    }

    public void resetExecutors(){
        close();
        initialise();
    }

    @Override
    public void close() {
        inputHandler.executor.shutdownNow();
        processHandler.executor.shutdownNow();
        outputHandler.executor.shutdownNow();
    }

    private OutputHandler createOutputHandler() {
        return new OutputHandler(createExecutor(1, new LinkedBlockingQueue<Runnable>()));
    }

    private ProcessHandler createProcessHandler() {
        return new ProcessHandler(createExecutor(1, new LinkedBlockingQueue<Runnable>(50)));
    }

    private InputHandler createInputHandler() {
        return new InputHandler(createExecutor(10, new LinkedBlockingQueue<Runnable>()));
    }

    public static ThreadPoolExecutor createExecutor(int threads, LinkedBlockingQueue<Runnable> workQueue) {
        return new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                workQueue,
                new BlockingRetryRejectedExecutionHandler());
    }

}
