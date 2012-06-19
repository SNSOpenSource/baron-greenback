package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.yadic.Container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;
import static java.util.Collections.unmodifiableMap;

public class HttpJob implements StagedJob<Response> {
    protected final Map<String, Object> context;

    protected HttpJob(Map<String, Object> context) {
        this.context = unmodifiableMap(context);
    }

    public static HttpJob job(HttpDataSource dataSource, Definition destination) {
        ConcurrentMap<String, Object> context = new ConcurrentHashMap<String, Object>();
        context.put("dataSource", dataSource);
        context.put("destination", destination);
        return new HttpJob(context);
    }

    public HttpDataSource dataSource() {
        return (HttpDataSource) context.get("dataSource");
    }

    public Definition destination() {
        return (Definition) context.get("destination");
    }

    @Override
    public Function<Response> getInput(Container container) {
        return asFunction(container.get(HttpClient.class)).deferApply(
                get(dataSource().uri()).build()).then(
                container.get(FailureHandler.class).captureFailures(dataSource()));
    }

    @Override
    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>> process(Container container) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob<Response>>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob<Response>>> call(Response response) throws Exception {
                final DocumentProcessor processed = new DocumentProcessor(loadDocument(response), dataSource(), destination(), Predicates.<Record>always());
                return Unchecked.cast(Pair.pair(processed.merged(), processed.subfeedJobs()));
            }
        };
    }

    @Override
    public Function1<Sequence<Record>, Number> write(final Application application) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(final Sequence<Record> newData) throws Exception {
                return application.usingRequestScope(new Callable1<Container, Number>() {
                    @Override
                    public Number call(Container container) throws Exception {
                        try {
                            return new DataWriter(container.get(BaronGreenbackRecords.class).value()).writeUnique(destination(), newData);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        };
    }
}
