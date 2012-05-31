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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;
import static java.util.Collections.unmodifiableMap;

public class HttpJob {
    protected final Map<String, Object> context;
    protected final HttpClient httpClient;
    protected final BlockingDeque<Pair<HttpDataSource, Response>> retry;

    protected HttpJob(Map<String, Object> context, HttpClient httpClient, BlockingDeque<Pair<HttpDataSource, Response>> retry) {
        this.context = unmodifiableMap(context);
        this.httpClient = httpClient;
        this.retry = retry;
    }

    public static HttpJob job(HttpDataSource dataSource, Definition destination) {
        ConcurrentMap<String, Object> context = new ConcurrentHashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);
        return new HttpJob(context, new ClientHttpHandler(), new LinkedBlockingDeque<Pair<HttpDataSource, Response>>());
    }

    public HttpDataSource dataSource() {
        return (HttpDataSource) context.get("dataSource");
    }

    public Definition destination() {
        return (Definition) context.get("destination");
    }

    public Function<Response> getInput(PrintStream log) {
        return asFunction(new AuditHandler(httpClient, new PrintAuditor(log))).deferApply(
                get(dataSource().uri()).build()).then(
                new FailureHandler(retry).captureFailures(dataSource()));
    }

    public Function1<Response, Pair<Sequence<Record>, Sequence<HttpJob>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<HttpJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<HttpJob>> call(Response response) throws Exception {
                Document document = loadDocument(response);
                Option<PaginatedHttpJob> jobs = additionalWork(destination(), document);
                Sequence<Record> records = transformData(document, dataSource().definition());
                Sequence<Record> filtered = filter().apply(records);
                Sequence<HttpJob> moreJobs = Subfeeder2.subfeeds(filtered, destination());
                Sequence<Record> merged = Subfeeder2.mergePreviousUniqueIdentifiers(filtered, dataSource());
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
