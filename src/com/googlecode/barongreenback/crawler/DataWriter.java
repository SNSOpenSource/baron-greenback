package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.barongreenback.crawler.executor.JobExecutor;
import com.googlecode.barongreenback.crawler.executor.PriorityJobRunnable;
import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.concurrent.NamedExecutors;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.PriorityMerge.priorityMergeBy;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
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
        ScheduledExecutorService service = NamedExecutors.newScheduledThreadPool(threads, getClass().getName());
        service.scheduleWithFixedDelay(processWork(application), 0, seconds, TimeUnit.SECONDS);
        return service;
    }

    public static Function1<Sequence<Record>, Number> write(final Job job, final Container container) {
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

    public int queue(Job job, Sequence<Record> newData, CountLatch latch) throws InterruptedException {
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
        List<Triple<Definition, Sequence<Record>, CountLatch>> newDataToWrite = new LinkedList<Triple<Definition, Sequence<Record>, CountLatch>>();
        data.drainTo(newDataToWrite);

        sequence(newDataToWrite).
                groupBy(first(Definition.class).then(Definition.functions.name)).
                eachConcurrently(new Block<Group<String, Triple<Definition, Sequence<Record>, CountLatch>>>() {
                    @Override
                    public void execute(final Group<String, Triple<Definition, Sequence<Record>, CountLatch>> newDataGroupedByDefinitionName) throws Exception {
                        newDataGroupedByDefinitionName.
                                groupBy(firstUniqueField()).
                                forEach(new Block<Group<Keyword<?>, Triple<Definition, Sequence<Record>, CountLatch>>>() {
                                    @Override
                                    public void execute(Group<Keyword<?>, Triple<Definition, Sequence<Record>, CountLatch>> newDataGroupedByUnique) throws Exception {
                                        Keyword<?> uniqueField = newDataGroupedByUnique.key();
                                        String definitionName = newDataGroupedByDefinitionName.key();
                                        Definition mergedDefinition = definition(definitionName, mergeFields(newDataGroupedByUnique));

                                        Sequence<Record> mergedRecords = priorityMergeBy(newDataGroupedByUnique.flatMap(Callables.<Sequence<Record>>second()), uniqueField);

                                        records.put(mergedDefinition, update(using(uniqueField), mergedRecords));
                                    }
                                });
                    }
                });

        for (Triple<Definition, Sequence<Record>, CountLatch> aNewData : newDataToWrite) {
            aNewData.third().countDown();
        }
    }

    private Callable1<Triple<Definition, Sequence<Record>, CountLatch>, Keyword<?>> firstUniqueField() {
        return new Callable1<Triple<Definition, Sequence<Record>, CountLatch>, Keyword<?>>() {
            @Override
            public Keyword<?> call(Triple<Definition, Sequence<Record>, CountLatch> triple) throws Exception {
                return uniqueFields(triple.first()).head();
            }
        };
    }

    private Set<Keyword<?>> mergeFields(Group<?, Triple<Definition, Sequence<Record>, CountLatch>> data) {
        final Set<Keyword<?>> mergedFields = new LinkedHashSet<Keyword<?>>();
        for (Triple<Definition, Sequence<Record>, CountLatch> trio : data) {
            mergedFields.addAll(trio.first().fields());
        }
        return mergedFields;
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
