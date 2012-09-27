package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.DataWriter.write;
import static com.googlecode.barongreenback.crawler.HttpReader.getInput;

public class StagedJobExecutor {
    private final JobExecutor inputHandler;
    private final JobExecutor processHandler;
    private final JobExecutor outputHandler;
    private final CountLatch latch;
    private final Container crawlerScope;

    public StagedJobExecutor(CrawlerExecutors executors, CountLatch latch, Container crawlerScope) {
        this.latch = latch;
        this.crawlerScope = crawlerScope;
        this.inputHandler = executors.inputHandler();
        this.processHandler = executors.processHandler();
        this.outputHandler = executors.outputHandler();
    }

    public int crawlAndWait(StagedJob job) throws InterruptedException {
        crawl(job);
        latch.await();
        return crawlerScope.get(AtomicInteger.class).get();
    }

    public void crawl(StagedJob job) throws InterruptedException {
        submit(inputHandler, getInput(job, crawlerScope).then(
                submit(processHandler, processJobs(job, crawlerScope).then(
                        submit(outputHandler, write(job, crawlerScope))))));
    }

    private void submit(JobExecutor jobExecutor, final Runnable function) {
        latch.countUp();
        jobExecutor.execute(logExceptions(countLatchDownAfter(function), crawlerScope.get(PrintStream.class)));
    }

    private <T> Function1<T, Void> submit(final JobExecutor jobExecutor, final Function1<T, ?> runnable) {
        return new Function1<T, Void>() {
            @Override
            public Void call(T result) throws Exception {
                submit(jobExecutor, runnable.deferApply(result));
                return Runnables.VOID;
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

    private Function1<Response, Sequence<Record>> processJobs(final StagedJob job, final Container scope) {
        return new Function1<Response, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Response t) throws Exception {
                Pair<Sequence<Record>, Sequence<StagedJob>> pair = job.process(scope, t);
                for (StagedJob job : pair.second()) {
                    crawl(job);
                }
                return pair.first();
            }
        };
    }
}
