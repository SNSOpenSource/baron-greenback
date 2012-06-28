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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;

public class QueuesCrawler extends AbstractCrawler {
    private final InputHandler inputHandler;
    private final ProcessHandler processHandler;
    private final OutputHandler outputHandler;
    private final Application application;
    private final PrintStream log;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final CrawlerFailures retry;

    public QueuesCrawler(final ModelRepository modelRepository, final Application application, InputHandler inputHandler,
                         ProcessHandler processHandler, OutputHandler outputHandler, CheckPointHandler checkpointHandler,
                         StringMappings mappings, CrawlerFailures retry, PrintStream log) {
        super(modelRepository);
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

        updateView(crawler, destination.fields());

        HttpDatasource datasource = HttpDatasource.dataSource(from(crawler), source);

        Container crawlContainer = crawlContainer(id, crawler);

        crawl(masterPaginatedHttpJob(crawlContainer, datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));

        crawlContainer.get(CountLatch.class).await();

        return crawlContainer.get(AtomicInteger.class).get();
    }
 

    private Container crawlContainer(UUID id, Model crawler) {
        Container container = new SimpleContainer();
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.add(HttpHandler.class, ClientHttpHandler.class);
        container.add(HttpClient.class, AuditHandler.class);
        container.addInstance(CrawlerFailures.class, retry);
        container.add(FailureHandler.class);
        container.add(CountLatch.class);
        container.addInstance(AtomicInteger.class, new AtomicInteger(0));
        container.addInstance(CheckpointUpdater.class, new CheckpointUpdater(checkpointHandler, id, crawler));
        return container;
    }

    public Future<?> crawl(StagedJob<Response> job) {
        return submit(inputHandler, HttpReader.getInput(job).then(
                submit(processHandler, processJobs(job.process()).then(
                        submit(outputHandler, DataWriter.write(application, job), job.container())), job.container())), job.container());
    }

    private Future<?> submit(JobExecutor jobExecutor, final Runnable runnable, final Container container) {
        container.get(CountLatch.class).countUp();
        return jobExecutor.executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    e.printStackTrace(container.get(PrintStream.class));
                    throw e;
                } finally {
                    container.get(CountLatch.class).countDown();
                }
            }
        });
    }

    private <T> Function1<T, Future<?>> submit(final JobExecutor jobExecutor, final Function1<T, ?> then, final Container container) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                container.get(CountLatch.class).countUp();
                return jobExecutor.executor.submit((Runnable) then.deferApply(result).then(new Callable1<Object, Object>() {
                    @Override
                    public Object call(Object o) throws Exception {
                        container.get(CountLatch.class).countDown();
                        return o;
                    }
                }));
            }
        };
    }

    private <T> Function1<T, Sequence<Record>> processJobs(final Function1<T, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> function) {
        return new Function1<T, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(T t) throws Exception {
                Pair<Sequence<Record>, Sequence<StagedJob<Response>>> pair = function.call(t);
                for (StagedJob<Response> job : pair.second()) {
                    crawl(job);
                }
                return pair.first();
            }
        };
    }
}