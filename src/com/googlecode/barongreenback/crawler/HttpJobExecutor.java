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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.DataWriter.write;
import static com.googlecode.barongreenback.crawler.HttpReader.getInput;

public class HttpJobExecutor {
    private final CrawlerExecutors executors;
    private final Map<UUID, Set<UUID>> latches = new ConcurrentHashMap<UUID, Set<UUID>>();
    private final Container crawlerScope;

    public HttpJobExecutor(CrawlerExecutors executors, Container crawlerScope) {
        this.executors = executors;
        this.crawlerScope = crawlerScope;
    }

    public int executeAndWait(Job job) throws InterruptedException {
        latches.put(job.crawlerId(), new ConcurrentSkipListSet<UUID>());
        final Phaser phaser = crawlerScope.get(Phaser.class);
        phaser.register();
        execute(job);
        while(!latches.get(job.crawlerId()).isEmpty()) {
            Thread.sleep(500);
        }
        phaser.awaitAdvance(phaser.arriveAndDeregister());
        return crawlerScope.get(AtomicInteger.class).get();
    }

    public void execute(Job job) throws InterruptedException {
        submit(job, executors.inputHandler(job), getInput(job, crawlerScope).then(
                submit(job, executors.processHandler(job), processJobs(job, crawlerScope).then(
                        submit(job, executors.outputHandler(job), write(job, crawlerScope))))));
    }

    private void submit(Job job, JobExecutor<PriorityJobRunnable> jobExecutor, final Runnable function) {
        final PriorityJobRunnable countDownLatch = countLatchDownAfter(job, function);
        latches.get(job.crawlerId()).add(countDownLatch.id());
        Runnable logExceptionsRunnable = logExceptions(countDownLatch, crawlerScope.get(PrintStream.class));
        PriorityJobRunnable priorityJobRunnable = new PriorityJobRunnable(job, logExceptionsRunnable);
        jobExecutor.execute(priorityJobRunnable);
    }

    private <T> Block<T> submit(final Job job, final JobExecutor<PriorityJobRunnable> jobExecutor, final Function1<T, ?> runnable) {
        return new Block<T>() {
            @Override
            public void execute(T result) throws Exception {
                submit(job, jobExecutor, runnable.interruptable().deferApply(result));
            }
        };
    }

    private PriorityJobRunnable countLatchDownAfter(final Job job, final Runnable function) {
        return new PriorityJobRunnable(job, function) {
            @Override
            public void run() {
                try {
                    function.run();
                } finally {
                    latches.get(job.crawlerId()).remove(this.id());
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
