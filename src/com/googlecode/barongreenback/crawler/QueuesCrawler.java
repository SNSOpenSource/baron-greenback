package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.*;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.generics.TypeFor;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.*;

import static com.googlecode.barongreenback.crawler.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.totallylazy.Runnables.VOID;

public class QueuesCrawler extends AbstractCrawler {
    private final ExecutorService inputHandlers;
    private final ExecutorService dataMappers;
    private final ExecutorService writers;
    private final Records records;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final BlockingDeque<Pair<HttpDataSource,Response>> retry;

    public QueuesCrawler(final ModelRepository modelRepository, final BaronGreenbackRecords records, CheckPointHandler checkpointHandler, StringMappings mappings) {
        super(modelRepository);
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.inputHandlers = Executors.newFixedThreadPool(20);
        this.dataMappers = Executors.newCachedThreadPool();
        this.writers = Executors.newSingleThreadExecutor();
        this.records = records.value();
        this.retry = new LinkedBlockingDeque<Pair<HttpDataSource, Response>>();
    }

    @Override
    public Number crawl(final UUID id, PrintStream log) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);

        updateView(crawler, keywords(destination));

        HttpDataSource dataSource = HttpDataSource.dataSource(requestFor(crawler), source);

        Container container = new SimpleContainer();
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.add(HttpHandler.class, ClientHttpHandler.class);
        container.add(HttpClient.class, AuditHandler.class);
        container.addType(new TypeFor<BlockingQueue<Pair<HttpDataSource, Response>>>(){}.get(), returns(retry));
        container.add(FailureHandler.class);
        container.addInstance(CheckpointUpdater.class, new CheckpointUpdater(new Function1<Option<Object>, Void>() {
            @Override
            public Void call(Option<Object> checkpoint) throws Exception {
                checkpointHandler.updateCheckPoint(id, crawler, checkpoint);
                return VOID;
            }
        }));

        crawl(paginatedHttpJob(dataSource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings), container);
        return -1;
    }

    private <T> Resolver<T> returns(final T instance) {
        return new Resolver<T>() {
            @Override
            public T resolve(Type type) throws Exception {
                return instance;
            }
        };
    }

    public static Uri requestFor(Model crawler) {
        return from(crawler);
    }

    public Future<?> crawl(StagedJob<Response> job, Container container) {
        return submit(inputHandlers, job.getInput(container).then(
                submit(dataMappers, processJobs(job.process(container), container).then(
                        submit(writers, job.write(records))))), container);
    }

    private Future<?> submit(ExecutorService executorService, final Runnable runnable, final Container container) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    e.printStackTrace(container.get(PrintStream.class));
                    throw e;
                }
            }
        });
    }

    private <T> Function1<T, Future<?>> submit(final ExecutorService executorService, final Function1<T, ?> then) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return executorService.submit((Runnable) then.deferApply(result));
            }
        };
    }

    private <T> Function1<T, Sequence<Record>> processJobs(final Function1<T, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> function, final Container container) {
        return new Function1<T, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(T t) throws Exception {
                Pair<Sequence<Record>, Sequence<StagedJob<Response>>> pair = function.call(t);
                for (StagedJob<Response> job : pair.second()) {
                    crawl(job, container);
                }
                return pair.first();
            }
        };
    }
}