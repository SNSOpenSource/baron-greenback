package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static java.util.Collections.unmodifiableMap;

public class HttpJob implements StagedJob {
    protected final Map<String, Object> context;
    private final Container container;

    protected HttpJob(Container container, Map<String, Object> context) {
        this.container = container;
        this.context = unmodifiableMap(context);

    }

    public static HttpJob job(Container container, HttpDatasource datasource, Definition destination) {
        ConcurrentMap<String, Object> context = new ConcurrentHashMap<String, Object>();
        context.put("dataSource", datasource);
        context.put("destination", destination);
        return new HttpJob(container, context);
    }

    @Override
    public Container container() {
        return container;
    }

    @Override
    public HttpDatasource dataSource() {
        return (HttpDatasource) context.get("dataSource");
    }

    @Override
    public Definition destination() {
        return (Definition) context.get("destination");
    }

    @Override
    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process() {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                return SubfeedJobCreator.process(container, dataSource(), destination(), transformData(loadDocument(response), dataSource().source()).realise());
            }
        };
    }
}