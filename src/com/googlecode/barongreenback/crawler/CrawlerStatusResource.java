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

    public CrawlerStatusResource(InputHandler inputExecutor, DataMapper mapperExecutor, PersistentDataWriter writerExecutor) {
        this.inputExecutor = inputExecutor;
        this.mapperExecutor = mapperExecutor;
        this.writerExecutor = writerExecutor;
    }

    @GET
    @Path("status")
    public Model status() {
        List<Model> executors = sequence(inputExecutor, mapperExecutor,  writerExecutor).safeCast(JobExecutor.class).map(toModel()).toList();
        return model().add("executors", executors);
    }

    private Callable1<JobExecutor, Model> toModel() {
        return new Callable1<JobExecutor, Model>() {
            @Override
            public Model call(JobExecutor executor) throws Exception {
                return model().add("name", executor.name()).add("size", executor.size());
            }
        };
    }


}
