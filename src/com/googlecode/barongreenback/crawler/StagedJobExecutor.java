package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.barongreenback.crawler.executor.InputHandler;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.barongreenback.crawler.executor.OutputHandler;
import com.googlecode.barongreenback.crawler.executor.ProcessHandler;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;

import java.io.PrintStream;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class StagedJobExecutor {
    private final InputHandler inputHandler;
    private final ProcessHandler processHandler;
    private final OutputHandler outputHandler;
    private final Application application;
    private final CountLatch latch = new CountLatch();
    private final Container container;

    public StagedJobExecutor(CrawlerExecutors executors, Application application, Container crawlContainer) {
        this.container = crawlContainer;
        this.inputHandler = executors.inputHandler();
        this.processHandler = executors.processHandler();
        this.outputHandler = executors.outputHandler();
        this.application = application;
    }

    public int crawlAndWait(StagedJob job) throws InterruptedException {
        crawl(job);
        latch.await();
        return container.get(AtomicInteger.class).get();
    }

    public Future<?> crawl(StagedJob job) throws InterruptedException {
        return submit(inputHandler, HttpReader.getInput(job, container).then(
                submit(processHandler, processJobs(job.process(container)).then(
                        submit(outputHandler, DataWriter.write(application, job, container))))));
    }

    private Future<?> submit(JobExecutor jobExecutor, final Runnable function) {
        latch.countUp();
        return jobExecutor.executor.submit(logExceptions(countLatchDownAfter(function), container.get(PrintStream.class)));
    }

    private <T> Function1<T, Future<?>> submit(final JobExecutor jobExecutor, final Function1<T, ?> runnable) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return submit(jobExecutor, runnable.deferApply(result));
            }
        };
    }

    private Runnable countLatchDownAfter(final Runnable function) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    function.run();
                } finally {
                    latch.countDown();
                }
            }
        };
    }

    private Runnable logExceptions(final Runnable function, final PrintStream printStream) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    function.run();
                } catch (RuntimeException e) {
                    e.printStackTrace(printStream);
                    throw e;
                }
            }
        };
    }

    private <T> Function1<T, Sequence<Record>> processJobs(final Function1<T, Pair<Sequence<Record>, Sequence<StagedJob>>> function) {
        return new Function1<T, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(T t) throws Exception {
                Pair<Sequence<Record>, Sequence<StagedJob>> pair = function.call(t);
                for (StagedJob job : pair.second()) {
                    crawl(job);
                }
                return pair.first();
            }
        };
    }
}