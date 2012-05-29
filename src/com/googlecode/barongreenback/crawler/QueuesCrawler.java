package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static com.googlecode.barongreenback.crawler.CheckPointStopper2.stopAt;
import static com.googlecode.barongreenback.crawler.PaginatedHttpDataSource.dataSource;
import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.barongreenback.crawler.Job.job;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class QueuesCrawler extends AbstractCrawler {
    private final ExecutorService httpHandlers;
    private final ExecutorService dataMappers;
    private final ExecutorService writers;
    private final Function1<Request, Response> httpHandler;
    private final FailureHandler failureHandler = new FailureHandler(new LinkedBlockingDeque<Pair<HttpDataSource, Response>>());
    private final DataWriter dataWriter;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;

    public QueuesCrawler(final ModelRepository modelRepository, final HttpClient httpClient, final BaronGreenbackRecords records, CheckPointHandler checkpointHandler, StringMappings mappings) {
        super(modelRepository);
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        httpHandler = asFunction(httpClient);
        httpHandlers = Executors.newFixedThreadPool(50);
        dataMappers = Executors.newCachedThreadPool();
        writers = Executors.newSingleThreadExecutor();
        this.dataWriter = new DataWriter(records);
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);

        updateView(crawler, keywords(destination));

        PaginatedHttpDataSource dataSource = dataSource(requestFor(crawler), source, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings);
        crawl(job(dataSource, destination));
        return -1;
    }

    public static Uri requestFor(Model crawler) {
        return from(crawler);
    }

    public static Request requestFor(Uri from) {
        return RequestBuilder.get(from).build();
    }

    public Future<?> crawl(Job job) {
        return submit(httpHandlers, get(job.dataSource().uri()).then(
                failureHandler.captureFailures(job.dataSource()).then(
                        submit(dataMappers, loadDocument().then(
                                processJobs(job.dataSource().additionalWork(job.destination()))).then(
                                transformData(job.dataSource().definition()).then(
                                        job.dataSource().filter()).then(
                                        processJobs(Subfeeder2.subfeeds(job.destination())).then(
                                                submit(writers, dataWriter.writeUnique(job.destination())))))))));
    }

    private Future<?> submit(ExecutorService executorService, Runnable runnable) {
        return executorService.submit(runnable);
    }

    private Function<Response> get(Uri uri) {
        return httpHandler.deferApply(requestFor(uri));
    }

    private <T> Function1<T, Future<?>> submit(final ExecutorService executorService, final Function1<T, ?> then) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return executorService.submit((Runnable) then.deferApply(result));
            }
        };
    }

    private <T> Function1<T, T> processJobs(final Function1<T, ? extends Iterable<Job>> function) {
        return new Function1<T, T>() {
            @Override
            public T call(T t) throws Exception {
                for (Job job : function.call(t)) {
                    crawl(job);
                }
                return t;
            }
        };
    }
}