package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.proxy.Invocation;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import com.googlecode.utterlyidle.jobs.JobsResource;
import com.googlecode.utterlyidle.schedules.ScheduleResource;

import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.Crawler.methods.extractRecordDefinition;
import static com.googlecode.barongreenback.shared.Forms.functions.addTemplates;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.Response.functions.asResponse;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

@Path("crawler")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
public class CrawlerDefinitionResource {
    private final Redirector redirector;
    private final CrawlInterval interval;
    private final Crawler crawler;
    private final PrintStream log;
    private final CrawlerRepository repository;
    private final ViewsRepository viewsRepository;

    public CrawlerDefinitionResource(CrawlerRepository repository, Redirector redirector, CrawlInterval interval, Crawler crawler, PrintStream log, ViewsRepository viewsRepository) {
        this.interval = interval;
        this.redirector = redirector;
        this.crawler = crawler;
        this.log = log;
        this.repository = repository;
        this.viewsRepository = viewsRepository;
    }

    @GET
    @Path("list")
    public Model list(@QueryParam("message") Option<String> message) {
        List<Model> models = repository.allCrawlerModels().map(asModelWithId()).toList();
        return message.fold(model().add("items", models), toMessageModel());
    }

    private Callable2<Model, String, Model> toMessageModel() {
        return new Callable2<Model, String, Model>() {
            @Override
            public Model call(Model model, String message) throws Exception {
                return model.add("message", model().add("text", message).add("category", "success"));
            }
        };
    }

    @GET
    @Path("export")
    public Response export(@QueryParam("id") UUID id) {
        return repository.modelFor(id).map(Callables.asString()).map(asResponse(MediaType.APPLICATION_JSON)).getOrElse(crawlerNotFound(id));
    }

    @GET
    @Path("import")
    public Model importForm() {
        return model();
    }

    @POST
    @Path("import")
    public Response importJson(@FormParam("model") String model, @FormParam("id") Option<UUID> id) {
        repository.importCrawler(id, model);
        return redirectToCrawlerList();
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id) {
        repository.remove(id);
        return redirectToCrawlerList();
    }

    @POST
    @Path("reset")
    public Response reset(@FormParam("id") UUID id) {
        repository.reset(id);
        return redirectToCrawlerList();
    }

    @GET
    @Path("new")
    public Model newForm() {
        return Forms.emptyForm(Forms.NUMBER_OF_FIELDS);
    }

    @POST
    @Path("new")
    public Response newCrawler(Model model) throws Exception {
        return edit(randomUUID(), model);
    }

    @POST
    @Path("copy")
    public Response copy(@FormParam("id") UUID id) throws Exception {
        return repository.copy(id).map(redirect()).getOrElse(crawlerNotFound(id));
    }

    @GET
    @Path("exists")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean exists(@QueryParam("id") UUID id) {
        return !repository.modelFor(id).isEmpty();
    }

    @GET
    @Path("edit")
    public Response edit(@QueryParam("id") final UUID id) {
        return repository.modelFor(id).map(addTemplates()).map(asResponse()).getOrElse(crawlerNotFound(id));
    }

    @POST
    @Path("edit")
    public Response edit(@QueryParam("id") UUID id, final Model root) throws Exception {
        repository.edit(id, root);
        return redirectToCrawlerList();
    }

    @POST
    @Path("crawl-and-create-view")
    @Produces(MediaType.TEXT_PLAIN)
    public Response crawlAndCreateView(@FormParam("id") final UUID id) throws Exception {
        createView(id);
        return crawl(id);
    }

    private void createView(UUID id) {
        final Model crawler = repository.crawlerFor(id);
        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);
        Sequence<Keyword<?>> keywords = Crawler.methods.keywords(recordDefinition);
        viewsRepository.ensureViewForCrawlerExists(crawler, keywords);
    }

    @POST
    @Path("crawl")
    @Produces(MediaType.TEXT_PLAIN)
    public Response crawl(@FormParam("id") final UUID id) throws Exception {
        return repository.modelFor(id).map(toNumberOfRecordsUpdated(id)).getOrElse(crawlerNotFound(id));
    }

    private Callable1<Model, Response> toNumberOfRecordsUpdated(final UUID id) {
        return new Callable1<Model, Response>() {
            @Override
            public Response call(Model model) throws Exception {
                if (repository.enabled(model)) {
                    return numberOfRecordsUpdated(crawler.crawl(id), log);
                }
                return forbidden(model);
            }

            private Response forbidden(Model model) {
                String name = model.get("form", Model.class).get("name", String.class);
                return response(Status.FORBIDDEN).entity(format("Crawler '%s' not enabled", name)).build();
            }
        };
    }

    @POST
    @Path("enable")
    public Response enable(@FormParam("id") UUID id) throws Exception {
        return repository.modelFor(id).map(enable(id, true)).getOrElse(crawlerNotFound(id));
    }

    @POST
    @Path("disable")
    public Response disable(@FormParam("id") UUID id) throws Exception {
        return repository.modelFor(id).map(enable(id, false)).getOrElse(crawlerNotFound(id));
    }

    private Callable1<Model, Response> enable(final UUID id, final boolean enabled) {
        return new Callable1<Model, Response>() {
            @Override
            public Response call(Model model) throws Exception {
                model.get("form", Model.class).set("disabled", !enabled);
                return edit(id, model);
            }
        };
    }

    private Callable1<? super Pair<UUID, Model>, Model> asModelWithId() {
        return new Callable1<Pair<UUID, Model>, Model>() {
            public Model call(Pair<UUID, Model> pair) throws Exception {
                return model().
                        add("id", pair.first().toString()).
                        add("model", pair.second()).
                        add("jobUrl", jobUrl(pair.first())).
                        add("resettable", hasCheckpoint(pair.second()));
            }
        };
    }

    private boolean hasCheckpoint(Model model) {
        return !Strings.isEmpty(model.get("form", Model.class).get("checkpoint", String.class));
    }

    private Uri jobUrl(UUID uuid) throws Exception {
        Uri scheduled = scheduleAQueuedCrawl(null, uuid, interval.value());
        return redirector.absoluteUriOf(scheduled);
    }

    public static Uri scheduleAQueuedCrawl(UUID crawlerId, UUID schedulerId, Long interval) throws Exception {
        String crawlerJob = absolutePathOf(method(on(CrawlerDefinitionResource.class).crawl(crawlerId)));
        String queued = absolutePathOf(method(on(JobsResource.class).create(null, crawlerJob)));
        return relativeUriOf(method(on(ScheduleResource.class).schedule(schedulerId, interval, queued)));
    }

    private static String absolutePathOf(Invocation<?, ?> method) {
        return "/" + relativeUriOf(method);
    }

    private Callable1<Model, Response> redirect() {
        return new Callable1<Model, Response>() {
            @Override
            public Response call(Model model) throws Exception {
                return redirectToCrawlerList();
            }
        };
    }

    private Response redirectToCrawlerList() {
        return redirector.seeOther(method(on(getClass()).list(Option.<String>none())));
    }


    private Response numberOfRecordsUpdated(Number updated, PrintStream log) {
        return response(Status.OK.description(format("OK - Updated %s Records", updated))).
                entity(log).
                build();
    }

    private Response crawlerNotFound(UUID id) {
        return response(Status.NOT_FOUND).entity(format("Crawler %s not found", id)).build();
    }

}