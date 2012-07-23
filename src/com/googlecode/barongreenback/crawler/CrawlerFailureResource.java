package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.QueryParam;
import com.googlecode.yadic.Container;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.Responses.response;

@Path("/crawler")
public class CrawlerFailureResource {
    private final CrawlerFailures crawlerFailures;
    private final Redirector redirector;
    private final CrawlerRepository crawlerRepository;
    private final Container requestScope;

    public CrawlerFailureResource(CrawlerFailures crawlerFailures, Redirector redirector, CrawlerRepository crawlerRepository, Container requestScope) {
        this.crawlerFailures = crawlerFailures;
        this.redirector = redirector;
        this.crawlerRepository = crawlerRepository;
        this.requestScope = requestScope;
    }

    @GET
    @Path("failures")
    public Model failures(@QueryParam("message") Option<String> message) {
        Model model = model().
                add("anyExists", !crawlerFailures.isEmpty()).
                add("failures", sequence(crawlerFailures.values().entrySet()).map(toModel()).toList());
        message.fold(model, toMessageModel())
                .add("retryUrl", redirector.absoluteUriOf(method(on(CrawlerFailureResource.class).retry(null))))
                .add("ignoreUrl", redirector.absoluteUriOf(method(on(CrawlerFailureResource.class).ignore(null))))
                .add("retryAll", redirector.absoluteUriOf(method(on(CrawlerFailureResource.class).retryAll())))
                .add("ignoreAll", redirector.absoluteUriOf(method(on(CrawlerFailureResource.class).ignoreAll())));
        return model;
    }

    private Callable2<Model, String, Model> toMessageModel() {
        return new Callable2<Model, String, Model>() {
            @Override
            public Model call(Model model, String s) throws Exception {
                return model.add("message", model().add("text", s).add("category", "success"));
            }
        };
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

    @POST
    @Path("failures/retryAll")
    public Response retryAll() {
        Set<UUID> uuids = crawlerFailures.values().keySet();
        int rowsToDelete = uuids.size();
        sequence(new HashSet<UUID>(uuids)).each(retry());
        return backToMe(rowsToDelete + " failures have been added to the job queue");
    }

    @POST
    @Path("failures/ignoreAll")
    public Response ignoreAll() {
        Set<UUID> uuids = crawlerFailures.values().keySet();
        int rowsToDelete = uuids.size();
        sequence(new HashSet<UUID>(uuids)).each(ignore());
        return backToMe(rowsToDelete + " failure(s) have been ignored");
    }

    private Callable1<UUID, Void> ignore() {
        return new Callable1<UUID, Void>() {
            @Override
            public Void call(UUID uuid) throws Exception {
                ignore(uuid);
                return Runnables.VOID;
            }
        };
    }

    private Callable1<UUID, Void> retry() {
        return new Callable1<UUID, Void>() {
            @Override
            public Void call(UUID uuid) throws Exception {
                retry(uuid);
                return Runnables.VOID;
            }
        };
    }


    private Callable1<Failure, Response> toIgnore(final UUID id) {
        return new Callable1<Failure, Response>() {
            @Override
            public Response call(Failure stagedJobResponsePair) throws Exception {
                crawlerFailures.delete(id);
                return backToMe("Job ignored");
            }
        };
    }

    private Callable1<Failure, Response> toRetry(final UUID id) {
        return new Callable1<Failure, Response>() {
            @Override
            public Response call(Failure failure) throws Exception {
                executor(failure.job()).crawl(failure.job());
                crawlerFailures.delete(id);
                return backToMe("Job retried");
            }
        };
    }

    private Response backToMe(String message) {
        return redirector.seeOther(method(on(CrawlerFailureResource.class).failures(some(message))));
    }

    private Callable1<Map.Entry<UUID, Failure>, Model> toModel() {
        return new Callable1<Map.Entry<UUID, Failure>, Model>() {
            @Override
            public Model call(Map.Entry<UUID, Failure> entry) throws Exception {
                return model().
                        add("job", entry.getValue().job()).
                        add("uri", entry.getValue().job().datasource().uri()).
                        add("reason", entry.getValue().reason()).
                        add("id", entry.getKey());
            }
        };
    }

    private StagedJobExecutor executor(StagedJob stagedJob) {
        CrawlerScope crawlerScope = CrawlerScope.crawlerScope(requestScope,
                new CheckpointUpdater(requestScope.get(CheckPointHandler.class), stagedJob.datasource().crawlerId(),
                        crawlerRepository.modelFor(stagedJob.datasource().crawlerId()).get()));
        return crawlerScope.get(StagedJobExecutor.class);
    }
}