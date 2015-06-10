package com.sky.sns.barongreenback.crawler.failures;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.QueryParam;
import com.googlecode.yadic.Container;
import com.sky.sns.barongreenback.crawler.CheckpointHandler;
import com.sky.sns.barongreenback.crawler.CheckpointUpdater;
import com.sky.sns.barongreenback.crawler.CrawlerRepository;
import com.sky.sns.barongreenback.crawler.CrawlerScope;
import com.sky.sns.barongreenback.crawler.HttpJobExecutor;
import com.sky.sns.barongreenback.crawler.jobs.Job;
import com.sky.sns.barongreenback.search.PredicateBuilder;
import com.sky.sns.barongreenback.shared.pager.Pager;
import com.sky.sns.barongreenback.shared.sorter.Sorter;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.Responses.response;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.CRAWLER_ID;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.DURATION;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.ID;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.JOB_TYPE;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.REASON;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.REQUEST_TIME;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.URI;
import static com.sky.sns.barongreenback.shared.sorter.Sorter.sortKeywordFromRequest;
import static java.lang.String.format;

@Path("/crawler/failures")
public class FailureResource {
    private final Failures failures;
    private final Redirector redirector;
    private final CrawlerRepository crawlerRepository;
    private final Container requestScope;
    private final Pager pager;
    private final Sorter sorter;
    private final FailureRepository failureRepository;
    private final PredicateBuilder predicateBuilder;
    public static final Sequence<Keyword<?>> HEADERS = Sequences.<Keyword<?>>sequence(URI, CRAWLER_ID, JOB_TYPE, REASON, REQUEST_TIME, DURATION);

    public FailureResource(Failures failures, FailureRepository failureRepository, Redirector redirector, CrawlerRepository crawlerRepository, Container requestScope, Pager pager, Sorter sorter, PredicateBuilder predicateBuilder) {
        this.failures = failures;
        this.redirector = redirector;
        this.crawlerRepository = crawlerRepository;
        this.requestScope = requestScope;
        this.pager = pager;
        this.sorter = sorter;
        this.failureRepository = failureRepository;
        this.predicateBuilder = predicateBuilder;
    }

    @GET
    @Path("list")
    public Model list(@QueryParam("message") final Option<String> message, @QueryParam("query") @DefaultValue("") final String query) {
        Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(query, HEADERS);
        if (invalidQueryOrPredicate.isLeft()) {
            return model().add("query", query).add("queryException", String.format("Cannot parse the query %s: %s", query, invalidQueryOrPredicate.left()));
        }
        Sequence<Record> unpaged = failureRepository.find(invalidQueryOrPredicate.right());
        Sequence<Record> sorted = sorter.sort(unpaged, sortKeywordFromRequest(HEADERS));
        Sequence<Record> paged = pager.paginate(sorted);
        Model model = pager.model(sorter.model(model().
                add("items", paged.map(toModel()).toList()), HEADERS, paged));
        return message.fold(model, toMessageModel()).
                add("query", query).
                add("retryUrl", redirector.absoluteUriOf(method(on(FailureResource.class).retry(null, query)))).
                add("deleteUrl", redirector.absoluteUriOf(method(on(FailureResource.class).delete(null, query)))).
                add("retryAll", redirector.absoluteUriOf(method(on(FailureResource.class).retryAll(query)))).
                add("deleteAll", redirector.absoluteUriOf(method(on(FailureResource.class).deleteAll(query))));
    }

    private Callable2<Model, String, Model> toMessageModel() {
        return new Callable2<Model, String, Model>() {
            @Override
            public Model call(Model model, String text) throws Exception {
                return model.add("message", model().add("text", text).add("category", "success"));
            }
        };
    }

    @POST
    @Path("retry")
    public Response retry(@FormParam("id") UUID id, @QueryParam("query") @DefaultValue("") final String query) {
        return failures.get(id).map(toRetry(id, query)).getOrElse(response(Status.NOT_FOUND));
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id, @QueryParam("query") @DefaultValue("") final String query) {
        return failures.get(id).map(toDelete(id, query)).getOrElse(response(Status.NOT_FOUND));
    }

    @POST
    @Path("retryAll")
    public Response retryAll(@QueryParam("query") @DefaultValue("") final String query) {
        final Predicate<Record> queryPredicate = getPredicateFromQuery(query);
        Sequence<UUID> uuids = failureRepository.find(queryPredicate).map(toUUID());
        int failuresToRetry = uuids.size();
        uuids.each(retry(query));
        return backToMe(failuresToRetry + " failures have been added to the job queue", query);
    }

    @POST
    @Path("deleteAll")
    public Response deleteAll(@QueryParam("query") @DefaultValue("") final String query) {
        final Predicate<Record> queryPredicate = getPredicateFromQuery(query);
        int deleted = failureRepository.remove(queryPredicate);
        return backToMe(deleted + " failure(s) have been deleted", query);
    }

    private Predicate<Record> getPredicateFromQuery(String query) {
        Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(query, HEADERS);
        if (invalidQueryOrPredicate.isLeft()) {
            throw new IllegalArgumentException(format("Invalid query %s", query));
        }
        return invalidQueryOrPredicate.right();
    }

    private Block<UUID> retry(final String query) {
        return new Block<UUID>() {
            @Override
            protected void execute(UUID uuid) throws Exception {
                retry(uuid, query);
            }
        };
    }

    private Mapper<Record, UUID> toUUID() {
        return new Mapper<Record, UUID>() {
            @Override
            public UUID call(Record record) throws Exception {
                return record.get(FailureRepository.ID);
            }
        };
    }


    private Callable1<Failure, Response> toDelete(final UUID id, final String query) {
        return new Callable1<Failure, Response>() {
            @Override
            public Response call(Failure stagedJobResponsePair) throws Exception {
                failures.delete(id);
                return backToMe("Job deleted", query);
            }
        };
    }

    private Callable1<Failure, Response> toRetry(final UUID id, final String query) {
        return new Callable1<Failure, Response>() {
            @Override
            public Response call(Failure failure) throws Exception {
                executor(failure.job()).execute(failure.job());
                failures.delete(id);
                return backToMe("Job retried", query);
            }
        };
    }

    private Response backToMe(final String message, final String query) {
        return redirector.seeOther(method(on(FailureResource.class).list(some(message), query)));
    }

    private Callable1<Record, Model> toModel() {
        return new Callable1<Record, Model>() {
            @Override
            public Model call(Record record) throws Exception {
                return model().
                        add("uri", record.get(URI)).
                        add("crawlerId", record.get(CRAWLER_ID)).
                        add("jobType", record.get(JOB_TYPE)).
                        add("reason", record.get(REASON)).
                        add("requestTime", record.get(REQUEST_TIME)).
                        add("duration", record.get(DURATION)).
                        add("id", record.get(ID));
            }
        };
    }

    private HttpJobExecutor executor(Job job) {
        CrawlerScope crawlerScope = CrawlerScope.crawlerScope(requestScope,
                new CheckpointUpdater(requestScope.get(CheckpointHandler.class), job.crawlerId(),
                        crawlerRepository.modelFor(job.crawlerId()).get()));
        return crawlerScope.get(HttpJobExecutor.class);
    }
}