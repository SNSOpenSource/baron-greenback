package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.util.Map;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("/crawler")
public class CrawlerFailureResource {
    private final CrawlerFailures crawlerFailures;
    private final Redirector redirector;
    private final QueuesCrawler crawler;

    public CrawlerFailureResource(CrawlerFailures crawlerFailures, Redirector redirector, Crawler crawler) {
        this.crawlerFailures = crawlerFailures;
        this.redirector = redirector;
        this.crawler = (QueuesCrawler) crawler;
    }

    @GET
    @Path("failures")
    public Model failures() {
        return model().add("failures", sequence(crawlerFailures.values().entrySet()).map(toFailureModel2()).toList());
    }

    private Callable1<Map.Entry<UUID, Pair<StagedJob<Response>, Response>>, Model> toFailureModel2() {
        return new Callable1<Map.Entry<UUID, Pair<StagedJob<Response>, Response>>, Model>() {
            @Override
            public Model call(Map.Entry<UUID, Pair<StagedJob<Response>, Response>> entry) throws Exception {
                return model().
                        add("job", entry.getValue().first()).
                        add("uri", entry.getValue().first().dataSource().uri()).
                        add("response", entry.getValue().second()).
                        add("id", entry.getKey());
            }
        };
    }

    @GET
    @Path("failures/retry")
    public Response retry(@QueryParam("id") UUID id) {
        Pair<StagedJob<Response>, Response> failure = crawlerFailures.get(id);
        crawler.crawl(failure.first());
        crawlerFailures.delete(id);
        return redirector.seeOther(method(on(CrawlerFailureResource.class).failures()));
    }
}
