package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsResource;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.post;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class BatchCrawlerResource {

    private RequestGenerator requestGenerator;
    private final ModelRepository modelRepository;
    private final Redirector redirector;
    private final Application application;

    public BatchCrawlerResource(final RequestGenerator requestGenerator, final ModelRepository modelRepository, Redirector redirector, Application application) {
        this.requestGenerator = requestGenerator;
        this.modelRepository = modelRepository;
        this.redirector = redirector;
        this.application = application;
    }


    @POST
    @Path("crawlAll")
    public Response crawlAll() throws Exception {
        return forAll(ids(), crawl());
    }

    @POST
    @Path("resetAll")
    public Response resetAll() throws Exception {
        return forAll(ids(), reset());
    }

    @POST
    @Path("deleteAll")
    public Response deleteAll() throws Exception {
        return forAll(ids(), delete());
    }

    private Sequence<UUID> ids() {
        return allCrawlerModels().map(first(UUID.class));
    }

    public static <T> Response forAll(final Sequence<T> sequence, final Callable1<T, Response> callable) throws Exception {
        return sequence.map(callable).last();
    }

    public Callable1<UUID, Response> crawl() {
        return new Callable1<UUID, Response>() {
            public Response call(UUID uuid) throws Exception {
                Uri crawlerUri = redirector.uriOf(method(on(CrawlerResource.class).crawl(uuid)));
                Uri jobsUri = redirector.uriOf(method(on(JobsResource.class).schedule(uuid, JobsResource.DEFAULT_INTERVAL, crawlerUri.path())));
                return application.handle(post(jobsUri).form("id", uuid).build());
            }
        };
    }

    public Callable1<UUID, Response> reset() {
        return new Callable1<UUID, Response>() {
            public Response call(UUID uuid) throws Exception {
                return application.handle(requestGenerator.requestFor(method(on(CrawlerResource.class).reset(uuid))));
            }
        };
    }

    public Callable1<UUID, Response> delete() {
        return new Callable1<UUID, Response>() {
            public Response call(UUID uuid) throws Exception {
                return application.handle(requestGenerator.requestFor(method(on(CrawlerResource.class).delete(uuid))));
            }
        };
    }

    private Sequence<Pair<UUID, Model>> allCrawlerModels() {
        return modelRepository.find(where(MODEL_TYPE, is("form")));
    }


}
