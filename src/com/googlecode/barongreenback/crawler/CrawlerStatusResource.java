package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.List;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Sequences.sequence;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerStatusResource {

    private final InputHandler inputExecutor;
    private final DataMapper mapperExecutor;
    private final PersistentDataWriter writerExecutor;

    private final RetryQueue retryQueue;

    public CrawlerStatusResource(InputHandler inputExecutor, DataMapper mapperExecutor, PersistentDataWriter writerExecutor, RetryQueue retryQueue) {
        this.inputExecutor = inputExecutor;
        this.mapperExecutor = mapperExecutor;
        this.writerExecutor = writerExecutor;
        this.retryQueue = retryQueue;
    }

    @GET
    @Path("status")
    public Model status() {
        List<Model> executors = sequence(inputExecutor, mapperExecutor,  writerExecutor, retryQueue).safeCast(StatusMonitor.class).map(toModel()).toList();
        return model().add("executors", executors);
    }

    private Callable1<StatusMonitor, Model> toModel() {
        return new Callable1<StatusMonitor, Model>() {
            @Override
            public Model call(StatusMonitor statusMonitor) throws Exception {
                return model().add("name", statusMonitor.name()).add("size", statusMonitor.size());
            }
        };
    }
}