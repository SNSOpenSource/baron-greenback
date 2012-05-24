package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.Handlers;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.utterlyidle.RequestBuilder.get;

public class QueuesCrawler extends AbstractCrawler {
    private final ExecutorService httpExecutors;
    private final ExecutorService dataMappers;
    private final ExecutorService writers;
    private final HttpClient httpClient;

    public QueuesCrawler(final ModelRepository modelRepository, final HttpClient httpClient) {
        super(modelRepository);
        this.httpClient = httpClient;
        httpExecutors = Executors.newFixedThreadPool(50);
        dataMappers = Executors.newCachedThreadPool();
        writers = Executors.newSingleThreadExecutor();
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        Model model = crawlerFor(id);
        RecordDefinition definition = extractRecordDefinition(model);
        final Request request = get(from(model)).build();
        httpExecutors.submit((Runnable) processRequest(definition, request));
        return -1;
    }

    private Runnable processRequest(RecordDefinition definition, Request request) {
        return Handlers.asFunction(httpClient).deferApply(request).then(submitForDataExtraction(definition));
    }

    private Function1<Response,Void> submitForDataExtraction(RecordDefinition definition) {
        return new Function1<Response, Void>() {
            @Override
            public Void call(Response response) throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Function<Response> handle(final Request request, final RecordDefinition definition) {
        throw new UnsupportedOperationException();
//        return new Callable<Response>() {
//            @Override
//            public Response call() throws Exception {
//                final Response response = httpClient.handle(request);
//                dataMappers.submit(new Callable<Object>() {
//                    @Override
//                    public Object call() throws Exception {
//                        Document document = document(response.entity().toString());
//                        Sequence<Record> records = new DocumentFeeder().get(document, definition);
//
//                        return records;
//                    }
//                });
//                return response;
//            }
//        };
    }
}
