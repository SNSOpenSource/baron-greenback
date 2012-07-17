package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.*;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;

public class QueuesCrawler extends AbstractCrawler {
    private final CrawlerHttpClient crawlerHttpHandler;
    private final InputHandler inputHandler;
    private final ProcessHandler processHandler;
    private final OutputHandler outputHandler;
    private final Application application;
    private final PrintStream log;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final CrawlerFailures retry;

    public QueuesCrawler(final ModelRepository modelRepository, final Application application, final CrawlerHttpClient crawlerHttpHandler, InputHandler inputHandler,
                         ProcessHandler processHandler, OutputHandler outputHandler, CheckPointHandler checkpointHandler,
                         StringMappings mappings, CrawlerFailures retry, PrintStream log) {
        super(modelRepository);
        this.crawlerHttpHandler = crawlerHttpHandler;
        this.inputHandler = inputHandler;
        this.processHandler = processHandler;
        this.outputHandler = outputHandler;
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.retry = retry;
        this.application = application;
        this.log = log;
    }

    @Override
    public Number crawl(final UUID id) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);
        checkOnlyOne(destination);

        updateView(crawler, destination.fields());

        HttpDatasource datasource = HttpDatasource.dataSource(from(crawler), source);

        Container crawlContainer = crawlContainer(id, crawler);

        crawl(masterPaginatedHttpJob(crawlContainer, datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));

        crawlContainer.get(CountLatch.class).await();

        return crawlContainer.get(AtomicInteger.class).get();
    }


    public Future<?> crawl(StagedJob job) {
        return submit(inputHandler, HttpReader.getInput(job).then(
                submit(processHandler, processJobs(job.process()).then(
                        submit(outputHandler, DataWriter.write(application, job), job.container())), job.container())), job.container());
    }

    private Container crawlContainer(UUID id, Model crawler) {
        Container container = new SimpleContainer();
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.addInstance(HttpHandler.class, crawlerHttpHandler);
        container.add(HttpClient.class, AuditHandler.class);
        container.addInstance(CrawlerFailures.class, retry);
        container.add(FailureHandler.class);
        container.add(CountLatch.class);
        container.addInstance(AtomicInteger.class, new AtomicInteger(0));
        container.addInstance(CheckpointUpdater.class, new CheckpointUpdater(checkpointHandler, id, crawler));
        return container;
    }

    private Future<?> submit(JobExecutor jobExecutor, final Runnable function, final Container container) {
        container.get(CountLatch.class).countUp();
        return jobExecutor.executor.submit(logExceptions(countLatchDownAfter(function, container.get(CountLatch.class)), container.get(PrintStream.class)));
    }

    private <T> Function1<T, Future<?>> submit(final JobExecutor jobExecutor, final Function1<T, ?> runnable, final Container container) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return submit(jobExecutor, runnable.deferApply(result), container);
            }
        };
    }

    private Runnable countLatchDownAfter(final Runnable function, final CountLatch countLatch) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    function.run();
                } finally {
                    countLatch.countDown();
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

    private Sequence<Keyword<?>> checkOnlyOne(Definition definition) {
        Sequence<Keyword<?>> uniques = definition.fields().filter(RecordDefinition.UNIQUE_FILTER);
        if(uniques.size() != 1) {
            throw new IllegalStateException("There should be exactly 1 unique field, instead there are " + uniques.size() + " (" + uniques + ").\n" +
                    "Please correct the crawler definition.");
        }
        return uniques;
    }
}