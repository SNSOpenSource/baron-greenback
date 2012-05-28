package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Uri;
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
import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class QueuesCrawler extends AbstractCrawler {
    private final ExecutorService httpHandlers;
    private final ExecutorService dataMappers;
    private final ExecutorService writers;
    private final Function1<Request, Response> httpHandler;
    private final FailureHandler failureHandler = new FailureHandler(new LinkedBlockingDeque<Pair<Request, Response>>());
    private final DataWriter dataWriter;
    private final CheckPointHandler checkPointHandler;
    private final MoreDataCrawler moreDataCrawler;

    public QueuesCrawler(final ModelRepository modelRepository, final HttpClient httpClient, final BaronGreenbackRecords records, CheckPointHandler checkPointHandler, StringMappings mappings) {
        super(modelRepository);
        this.checkPointHandler = checkPointHandler;
        httpHandler = asFunction(httpClient);
        httpHandlers = Executors.newFixedThreadPool(50);
        dataMappers = Executors.newCachedThreadPool();
        writers = Executors.newSingleThreadExecutor();
        this.dataWriter = new DataWriter(records);
        this.moreDataCrawler = new MoreDataCrawler(mappings, this);
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);

        updateView(crawler, keywords(destination));

        crawl(requestFor(crawler), source, destination, checkPointHandler.lastCheckPointFor(crawler), more(crawler));
        return -1;
    }

    public static Request requestFor(Model crawler) {
        return requestFor(from(crawler));
    }

    public static Request requestFor(Uri from) {
        return RequestBuilder.get(from).build();
    }

    public Future<?> crawl(Request request, Definition source, Definition destination, Object checkpoint, String more) {
        return submit(httpHandlers, get(request).then(
                failureHandler.captureFailures(request).then(
                submit(dataMappers, loadDocument().then(
                moreDataCrawler.getMoreIfNeeded(source, destination, checkpoint, more)).then(
                transformData(source).then(stopAt(checkpoint)).then(
                submit(writers, dataWriter.writeUnique(destination))))))));
    }

    private Future<?> submit(ExecutorService executorService, Runnable runnable) {
        return executorService.submit(runnable);
    }

    private Function<Response> get(Request request) {
        return httpHandler.deferApply(request);
    }

    private <T> Function1<T, Future<?>> submit(final ExecutorService executorService, final Function1<T, ?> then) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return executorService.submit((Runnable) then.deferApply(result));
            }
        };
    }
}