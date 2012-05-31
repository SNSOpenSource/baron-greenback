package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import org.w3c.dom.Document;

import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingDeque;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class HttpJob {
    private final FailureHandler failureHandler;
    protected final HttpDataSource dataSource;
    private final Definition destination;
    protected final HttpClient httpHandler;

    public HttpJob(HttpDataSource dataSource, Definition destination, HttpClient httpHandler) {
        this.dataSource = dataSource;
        this.destination = destination;
        this.httpHandler = httpHandler;
        this.failureHandler = new FailureHandler(new LinkedBlockingDeque<Pair<HttpDataSource, Response>>());
    }

    public static HttpJob job(HttpDataSource dataSource, Definition destination) {
        return new HttpJob(dataSource, destination, new ClientHttpHandler());
    }

    public HttpDataSource dataSource() {
        return dataSource;
    }

    public Definition destination() {
        return destination;
    }

    public Function<Response> getInput(PrintStream log) {
        return asFunction(new AuditHandler(httpHandler, new PrintAuditor(log))).deferApply(
                get(dataSource.uri()).build()).then(
                failureHandler.captureFailures(dataSource));
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<HttpJob>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<HttpJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<HttpJob>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                Option<PaginatedHttpJob> jobs = additionalWork(destination, document);
                Sequence<Record> records = transformData(document, dataSource.definition());
                Sequence<Record> filtered = filter().apply(records);
                Sequence<HttpJob> moreJobs = Subfeeder2.subfeeds(filtered, destination);
                Sequence<Record> merged = Subfeeder2.mergePreviousUniqueIdentifiers(filtered, dataSource);
                return Pair.pair(merged, moreJobs.join(jobs));
            }
        };
    }

    public Option<PaginatedHttpJob> additionalWork(final Definition destination, Document document) {
        return none();
    }

    public Function1<Sequence<Record>, Sequence<Record>> filter() {
        return Function1.identity();
    }
}
