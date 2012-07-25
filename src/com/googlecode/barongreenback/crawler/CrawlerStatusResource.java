package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.executor.CrawlerExecutors;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.List;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Sequences.sequence;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerStatusResource {

    private final CrawlerFailures crawlerFailures;
    private final CrawlerExecutors crawlerExecutors;

    public CrawlerStatusResource(CrawlerExecutors crawlerExecutors, CrawlerFailures crawlerFailures) {
        this.crawlerExecutors = crawlerExecutors;
        this.crawlerFailures = crawlerFailures;
    }

    @GET
    @Path("status")
    public Model status() {
        List<Model> executors = sequence(crawlerExecutors.inputHandler(), crawlerExecutors.processHandler(), crawlerExecutors.outputHandler(), crawlerFailures).safeCast(StatusMonitor.class).map(toModel()).toList();
        return model().add("executors", executors);
    }

    private Callable1<StatusMonitor, Model> toModel() {
        return new Callable1<StatusMonitor, Model>() {
            @Override
            public Model call(StatusMonitor statusMonitor) throws Exception {
                return model().
                        add("name", statusMonitor.name()).
                        add("size", statusMonitor.size()).
                        add("activeThreads", statusMonitor.activeThreads().map(asString(Integer.class)).getOrElse("N/A"));
            }
        };
    }
}