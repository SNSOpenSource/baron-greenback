package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.barongreenback.crawler.executor.PriorityJobRunnable;
import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.io.PrintStream;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.DataWriter.write;
import static com.googlecode.barongreenback.crawler.HttpReader.getInput;

public class HttpJobExecutor {
    private final CrawlerExecutors executors;
    private final Phaser latch;
    private final Container crawlerScope;

    public HttpJobExecutor(CrawlerExecutors executors, Phaser latch, Container crawlerScope) {
        this.executors = executors;
        this.latch = latch;
        this.crawlerScope = crawlerScope;
    }

    public int executeAndWait(Job job) throws InterruptedException {
        latch.register();
        execute(job);
        latch.awaitAdvance(latch.arriveAndDeregister());
        return crawlerScope.get(AtomicInteger.class).get();
    }

    public void execute(Job job) throws InterruptedException {
        submit(job, executors.inputHandler(job), getInput(job, crawlerScope).then(
						        submit(job, executors.processHandler(job), processJobs(job, crawlerScope).then(
										        submit(job, executors.outputHandler(job), write(job, crawlerScope))))));
    }

    private void submit(Job job, JobExecutor<PriorityJobRunnable> jobExecutor, final Runnable function) {
        latch.register();
        try {
            Runnable logExceptionsRunnable = logExceptions(countLatchDownAfter(function), crawlerScope.get(PrintStream.class));
            PriorityJobRunnable priorityJobRunnable = new PriorityJobRunnable(job, logExceptionsRunnable);
            jobExecutor.execute(priorityJobRunnable);
        } catch (RuntimeException e) {
            latch.arriveAndDeregister();
            throw e;
        }
    }

    private <T> Block<T> submit(final Job job, final JobExecutor<PriorityJobRunnable> jobExecutor, final Function1<T, ?> runnable) {
        return new Block<T>() {
            @Override
            public void execute(T result) throws Exception {
                submit(job, jobExecutor, runnable.interruptable().deferApply(result));
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
                    latch.arriveAndDeregister();
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

    private Function1<Response, Sequence<Record>> processJobs(final Job job, final Container scope) {
        return new Function1<Response, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Response t) throws Exception {
                Pair<Sequence<Record>, Sequence<Job>> pair = job.process(scope, t);
                for (Job job : pair.second().interruptable()) {
                    execute(job);
                }
                return pair.first().interruptable();
            }
        };
    }
}
