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
public class CrawlerExecutorConfigResource {

    private final CrawlerExecutors crawlerExecutors;

    public CrawlerExecutorConfigResource(CrawlerExecutors crawlerExecutors) {
        this.crawlerExecutors = crawlerExecutors;
    }

    @GET
    @Path("executors")
    public Model status() {
       return model().add("InputHandlerThreads", crawlerExecutors.getInputHandlerThreads()).
                add("InputHandlerCapacity", crawlerExecutors.getInputHandlerCapacity()).
                add("ProcessHandlerThreads", crawlerExecutors.getProcessHandlerThreads()).
                add("ProcessHandlerThreads", crawlerExecutors.getProcessHandlerCapacity()).
                add("OutputHandlerThreads", crawlerExecutors.getOutputHandlerThreads()).
                add("OutputHandlerThreads", crawlerExecutors.getOutputHandlerCapacity());
    }

}
