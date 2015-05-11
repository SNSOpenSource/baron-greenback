package com.sky.sns.barongreenback.crawler.jobs;

import com.sky.sns.barongreenback.crawler.datasources.DataSource;
import com.sky.sns.barongreenback.crawler.datasources.HttpDataSource;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.sky.sns.barongreenback.crawler.DataTransformer.loadDocument;
import static com.sky.sns.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.funclate.Model.persistent.model;

public class HttpJob implements Job {
    protected final Model context;

    protected HttpJob(Model context) {
        this.context = context;
    }

    public static HttpJob httpJob(UUID crawlerId, Record record, DataSource dataSource, Definition destination, Set<DataSource> visited, Date createdDate) {
        return httpJob(createContext(crawlerId, record, dataSource, destination, visited, createdDate));
    }

    public static HttpJob httpJob(Model model) {
        return new HttpJob(model);
    }

    protected static Model createContext(UUID crawlerId, Record record, DataSource datasource, Definition destination, Set<DataSource> visited, Date createdDate) {
        return model().
                set("record", record).
                set("crawlerId", crawlerId).
                set("datasource", datasource).
                set("destination", destination).
                set("visited", visited).
                set("createdDate", createdDate);
    }

    @Override
    public UUID crawlerId() {
        return context.get("crawlerId");
    }

    @Override
    public DataSource dataSource() {
        return context.get("datasource");
    }

    @Override
    public Definition destination() {
        return context.get("destination");
    }

    @Override
    public Set<DataSource> visited() {
        return context.get("visited");
    }

    @Override
    public Record record() {
        return context.get("record");
    }

    @Override
    public Date createdDate() {
        return context.get("createdDate");
    }

    @Override
    public Pair<Sequence<Record>, Sequence<Job>> process(final Container crawlerScope, Response response) throws Exception {
        return new HttpSubfeedJobCreator(destination(), visited(), crawlerId(), record(), createdDate()).process(transformData(loadDocument(response), dataSource().source()).realise());
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
        return String.format("datasource: %s, destination: %s", dataSource(), destination());
    }
}