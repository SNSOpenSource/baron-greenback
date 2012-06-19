package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.*;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.*;

import static com.googlecode.totallylazy.Runnables.VOID;

public class QueuesCrawler extends AbstractCrawler {
    private final InputHandler inputHandler;
    private final ProcessHandler processHandler;
    private final OutputHandler outputHandler;
    private final Application application;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final RetryQueue retry;

    public QueuesCrawler(final ModelRepository modelRepository, final Application application, InputHandler inputHandler,
                         ProcessHandler processHandler, OutputHandler outputHandler, CheckPointHandler checkpointHandler,
                         StringMappings mappings, RetryQueue retry) {
        super(modelRepository);
        this.inputHandler = inputHandler;
        this.processHandler = processHandler;
        this.outputHandler = outputHandler;
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.retry = retry;
        this.application = application;
    }

    @Override
    public Number crawl(final UUID id, PrintStream log) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);

        updateView(crawler, keywords(destination));

        HttpDataSource dataSource = HttpDataSource.dataSource(from(crawler), source);

        Container container = new SimpleContainer();
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.add(HttpHandler.class, ClientHttpHandler.class);
        container.add(HttpClient.class, AuditHandler.class);
        container.addInstance(RetryQueue.class, retry);
        container.add(FailureHandler.class);
        container.addInstance(CheckpointUpdater.class, new CheckpointUpdater(checkpointUpdater(id, crawler)));

        crawl(MasterPaginatedHttpJob.masterPaginatedHttpJob(dataSource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings), container);
        return -1;
    }

    public Future<?> crawl(StagedJob<Response> job, Container container) {
        return submit(inputHandler, job.getInput(container).then(
                submit(processHandler, processJobs(job.process(container), container).then(
                        submit(outputHandler, job.write(application))))), container);
    }

    private Function1<Option<?>, Void> checkpointUpdater(final UUID id, final Model crawler) {
        return new Function1<Option<?>, Void>() {
            @Override
            public Void call(Option<?> checkpoint) throws Exception {
                checkpointHandler.updateCheckPoint(id, crawler, checkpoint);
                return VOID;
            }
        };
    }

    private Future<?> submit(JobExecutor jobExecutor, final Runnable runnable, final Container container) {
        return jobExecutor.executor.submit(new Runnable() {
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

    private <T> Function1<T, Future<?>> submit(final JobExecutor jobExecutor, final Function1<T, ?> then) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return jobExecutor.executor.submit((Runnable) then.deferApply(result));
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