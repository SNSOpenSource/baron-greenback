package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequences;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CrawlerExecutors implements Closeable {
    private InputHandler inputHandler;
    private ProcessHandler processHandler;
    private OutputHandler outputHandler;

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

    private void initialise() {
        this.inputHandler = createInputHandler();
        this.processHandler = createProcessHandler();
        this.outputHandler = createOutputHandler();
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

    private OutputHandler createOutputHandler() {
        return new OutputHandler(createExecutor(1, new LinkedBlockingQueue<Runnable>()));
    }

    private ProcessHandler createProcessHandler() {
        return new ProcessHandler(createExecutor(1, new LinkedBlockingQueue<Runnable>(50)));
    }

    private InputHandler createInputHandler() {
        return new InputHandler(createExecutor(10, new LinkedBlockingQueue<Runnable>()));
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
