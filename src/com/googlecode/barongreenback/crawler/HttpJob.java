package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Set;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.DataTransformer.loadDocument;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.funclate.Model.immutable.model;

public class HttpJob implements StagedJob {
    protected final Model context;

    protected HttpJob(Model context) {
        this.context = context;
    }

    public static HttpJob httpJob(UUID crawlerId, Record record, HttpDatasource datasource, Definition destination, Set<HttpDatasource> visited) {
        return new HttpJob(createContext(crawlerId, record, datasource, destination, visited));
    }

    protected static Model createContext(UUID crawlerId, Record record, HttpDatasource datasource, Definition destination, Set<HttpDatasource> visited) {
        return model().
                set("record", record).
                set("crawlerId", crawlerId).
                set("datasource", datasource).
                set("destination", destination).
                set("visited", visited);
    }

    @Override
    public UUID crawlerId() {
        return context.get("crawlerId");
    }

    @Override
    public HttpDatasource datasource() {
        return context.get("datasource");
    }

    @Override
    public Definition destination() {
        return context.get("destination");
    }

    @Override
    public Set<HttpDatasource> visited() {
        return context.get("visited");
    }

    @Override
    public Record record() {
        return context.get("record");
    }

    @Override
    public Pair<Sequence<Record>, Sequence<StagedJob>> process(final Container crawlerScope, Response response) throws Exception {
        return new SubfeedJobCreator(destination(), visited(), crawlerId(), record()).process(transformData(loadDocument(response), datasource().source()).realise());
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