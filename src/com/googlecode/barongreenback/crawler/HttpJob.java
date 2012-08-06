package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static java.util.Collections.unmodifiableMap;

public class HttpJob implements StagedJob {
    protected final Map<String, Object> context;

    protected HttpJob(Map<String, Object> context) {
        this.context = unmodifiableMap(context);
    }

    public static HttpJob httpJob(HttpDatasource datasource, Definition destination, Set<HttpDatasource> visited) {
        ConcurrentMap<String, Object> context = createContext(datasource, destination, visited);
        return new HttpJob(context);
    }

    protected static ConcurrentMap<String, Object> createContext(HttpDatasource datasource, Definition destination, Set<HttpDatasource> visited) {
        ConcurrentMap<String, Object> context = new ConcurrentHashMap<String, Object>();
        context.put("datasource", datasource);
        context.put("destination", destination);
        context.put("visited", visited);
        return context;
    }

    @Override
    public HttpDatasource datasource() {
        return (HttpDatasource) context.get("datasource");
    }

    @Override
    public Definition destination() {
        return (Definition) context.get("destination");
    }

    @Override
    public Set<HttpDatasource> visited() {
        return (Set<HttpDatasource>) context.get("visited");
    }

    @Override
    public Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(final Container crawlerScope) {
        return new Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>>() {
            @Override
            public Pair<Sequence<Record>, Sequence<StagedJob>> call(Response response) throws Exception {
                return new SubfeedJobCreator(datasource(), destination(), visited()).process(transformData(loadDocument(response), datasource().source()).realise());
            }
        };
    }

    @Override
    public int hashCode() {
        return context.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof HttpJob) && ((HttpJob) other).context.equals(context);
    }

    @Override
    public String toString() {
        return String.format("datasource: %s, destination: %s", datasource(), destination());
    }
}