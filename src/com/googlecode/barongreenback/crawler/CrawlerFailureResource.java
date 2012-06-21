package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.*;

import java.util.Map;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.Responses.response;

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
        return model().add("failures", sequence(crawlerFailures.values().entrySet()).map(toModel()).toList());
    }

    @POST
    @Path("failures/retry")
    public Response retry(@FormParam("id") UUID id) {
        return crawlerFailures.get(id).map(toRetry(id)).getOrElse(response(Status.NOT_FOUND));
    }

    @POST
    @Path("failures/ignore")
    public Response ignore(@FormParam("id") UUID id) {
        return crawlerFailures.get(id).map(toIgnore(id)).getOrElse(response(Status.NOT_FOUND));
    }

    private Callable1<Pair<StagedJob<Response>, Response>, Response> toIgnore(final UUID id) {
        return new Callable1<Pair<StagedJob<Response>, Response>, Response>() {
            @Override
            public Response call(Pair<StagedJob<Response>, Response> stagedJobResponsePair) throws Exception {
                crawlerFailures.delete(id);
                return backToMe();
            }
        };
    }

    private Callable1<Pair<StagedJob<Response>, Response>, Response> toRetry(final UUID id) {
        return new Callable1<Pair<StagedJob<Response>, Response>, Response>() {
            @Override
            public Response call(Pair<StagedJob<Response>, Response> failure) throws Exception {
                crawler.crawl(failure.first());
                crawlerFailures.delete(id);
                return backToMe();

            }
        };
    }

    private Response backToMe() {
        return redirector.seeOther(method(on(CrawlerFailureResource.class).failures()));
    }

    private Callable1<Map.Entry<UUID, Pair<StagedJob<Response>, Response>>, Model> toModel() {
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
}
