package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.barongreenback.crawler.executor.PriorityJobRunnable;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.PriorityMerge.priorityMergeBy;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Sequences.sequence;

public class DataWriter implements JobExecutor<PriorityJobRunnable> {
    private final BlockingQueue<Triple<Definition, Sequence<Record>, CountLatch>> data;
    private final Application application;
    private final int threads;
    private final int seconds;
    private final String name;
    private final ExecutorService executor;
    private volatile boolean running = false;


    public DataWriter(final Application application, int threads, int seconds, String name, int capacity) {
        this.application = application;
        this.threads = threads;
        this.seconds = seconds;
        this.name = name;
        executor = createExecutor();
        data = new LinkedBlockingQueue<Triple<Definition, Sequence<Record>, CountLatch>>(capacity);
    }

    private ExecutorService createExecutor() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(threads);
        service.scheduleWithFixedDelay(processWork(application), 0, seconds, TimeUnit.SECONDS);
        return service;
    }

    public static Function1<Sequence<Record>, Number> write(final StagedJob job, final Container container) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(final Sequence<Record> newData) throws Exception {
                CountLatch latch = container.get(CountLatch.class);
                int count = container.get(CrawlerExecutors.class).outputHandler(job).queue(job, newData, latch);
                container.get(AtomicInteger.class).addAndGet(count);
                return count;
            }
        };
    }

    @Override
    public void execute(PriorityJobRunnable command) {
        command.run();
    }

    public int queue(StagedJob job, Sequence<Record> newData, CountLatch latch) throws InterruptedException {
        Definition destination = job.destination();
        if (newData.isEmpty()) return 0;

        Sequence<Keyword<?>> unique = uniqueFields(destination);
        if (!newData.head().fields().map(Callables.<Keyword<?>>first()).exists(in(unique))) {
            return 0;
        }

        latch.countUp();
        data.put(Triple.triple(destination, newData, latch));
        return newData.size();
    }

    private void batchWrite(final Records records) {
        List<Triple<Definition, Sequence<Record>, CountLatch>> newData = new ArrayList<Triple<Definition, Sequence<Record>, CountLatch>>();
        data.drainTo(newData);
        sequence(newData).groupBy(first(Definition.class)).mapConcurrently(new Function1<Group<Definition, Triple<Definition, Sequence<Record>, CountLatch>>, Number>() {
            @Override
            public Number call(Group<Definition, Triple<Definition, Sequence<Record>, CountLatch>> group) throws Exception {
                Definition definition = group.key();
                Keyword<?> unique = uniqueFields(definition).head();
                Sequence<Record> mergedRecords = priorityMergeBy(group.flatMap(Callables.<Sequence<Record>>second()), unique);
                return records.put(definition, update(using(unique), mergedRecords));
            }
        }).realise();
        for (Triple<Definition, Sequence<Record>, CountLatch> aNewData : newData) {
            aNewData.third().countDown();
        }
    }

    private Runnable processWork(final Application application) {
        return new Runnable() {
            @Override
            public void run() {
                running = true;
                application.usingRequestScope(new Block<Container>() {
                    @Override
                    protected void execute(Container container) throws Exception {
                        batchWrite(container.get(BaronGreenbackRecords.class).value());
                    }
                }.optional());
                running = false;
            }
        };
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Option<Integer> activeThreads() {
        return some(running ? 1 : 0);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
        data.clear();
    }
}
