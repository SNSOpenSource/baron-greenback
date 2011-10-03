package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsResource;
import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.DateFormatConverter;
import com.googlecode.totallylazy.Dates;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.Crawler.CHECKPOINT_VALUE;
import static com.googlecode.barongreenback.crawler.Crawler.MORE;
import static com.googlecode.barongreenback.crawler.Crawler.URL;
import static com.googlecode.barongreenback.jobs.JobsResource.DEFAULT_INTERVAL;
import static com.googlecode.barongreenback.shared.ModelRepository.ID;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerResource {
    private final Records records;
    private final ModelRepository modelRepository;
    private final Crawler crawler;
    private final Views views;
    private final Redirector redirector;

    public CrawlerResource(final Records records, final ModelRepository modelRepository, Crawler crawler, Views views, Redirector redirector) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.crawler = crawler;
        this.views = views;
        this.redirector = redirector;
    }

    @GET
    @Path("list")
    public Model list() {
        return model().add("items", modelRepository.find(where(ID, is(notNullValue()))).map(asModelWithId()).toList());
    }

    @GET
    @Path("export")
    @Produces("application/json")
    public String export(@QueryParam("id") UUID id) {
        return modelFor(id).toString();
    }

    @GET
    @Path("import")
    public Model importForm() {
        return model();
    }

    @POST
    @Path("import")
    public Response importJson(@FormParam("model") String model) {
        modelRepository.set(UUID.randomUUID(), Model.parse(model));
        return redirectToList();
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id) {
        modelRepository.remove(id);
        return redirectToList();
    }

    @GET
    @Path("new")
    public Model newForm() {
        return Forms.emptyForm(Forms.NUMBER_OF_FIELDS);
    }

    @POST
    @Path("new")
    public Response newCrawler(Model model) throws Exception {
        return edit(UUID.randomUUID(), model);
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") UUID id) {
        return Forms.addTemplates(modelFor(id));
    }

    @POST
    @Path("edit")
    public Response edit(@QueryParam("id") UUID id, final Model root) throws Exception {
        Model form = root.get("form", Model.class);
        String from = form.get("from", String.class);
        String update = form.get("update", String.class);
        String more = form.get("more", String.class);
        String checkpoint = form.get("checkpoint", String.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        modelRepository.set(id, Forms.form(update, from, more, checkpoint, recordDefinition.toModel()));
        return redirectToList();
    }

    @POST
    @Path("crawl")
    @Produces(MediaType.TEXT_PLAIN)
    public String crawl(@FormParam("id") UUID id) throws Exception {
        Model model = modelRepository.get(id);
        Model form = model.get("form", Model.class);
        String from = form.get("from", String.class);
        String update = form.get("update", String.class);
        String more = form.get("more", String.class);
        String checkpoint = form.get("checkpoint", String.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        Pair<Date, Sequence<Record>> newCheckpointAndRecords = crawler.crawl(crawlingDefinition(from, more, checkpoint, recordDefinition));
        System.out.println(String.format("Crawled %d new items for %s", newCheckpointAndRecords.second().size(), update));
        Date checkPoint = newCheckpointAndRecords.first();
        modelRepository.set(id, Forms.form(update, from, more, checkPoint != null ? checkPoint.toString() : "", recordDefinition.toModel()));
        return put(keyword(update), uniqueFields(recordDefinition), newCheckpointAndRecords.second());
    }

    private Record crawlingDefinition(String from, String more, String checkpoint, RecordDefinition recordDefinition) {
        return record().set(URL, url(from)).set(RecordDefinition.RECORD_DEFINITION, recordDefinition).set(MORE, more).set(CHECKPOINT_VALUE, toDate(checkpoint));
    }

    private Date toDate(String checkpoint) {
        if (checkpoint.isEmpty()) return null;
        return new DateFormatConverter(Dates.RFC3339(), Dates.RFC822(), Dates.javaUtilDateToString()).toDate(checkpoint);
    }

    private Model modelFor(UUID id) {
        return modelRepository.get(id);
    }

    private Callable1<? super Pair<UUID, Model>, Model> asModelWithId() {
        return new Callable1<Pair<UUID, Model>, Model>() {
            public Model call(Pair<UUID, Model> pair) throws Exception {
                return model().
                        add("id", pair.first().toString()).
                        add("model", pair.second()).
                        add("jobUrl", jobUrl(pair.first()));
            }
        };
    }

    private Uri jobUrl(UUID uuid) throws Exception {
        Uri uri = relativeUriOf(method(on(CrawlerResource.class).crawl(null)));
        return redirector.uriOf(method(on(JobsResource.class).schedule(uuid, DEFAULT_INTERVAL, "/" + uri.toString())));
    }

    private Response redirectToList() {
        return redirector.seeOther(method(on(getClass()).list()));
    }

    private String put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        if(recordsToAdd.isEmpty()){
            return numberOfRecordsUpdated(0);
        }
        Sequence<Keyword> keywords = keywords(recordsToAdd);
        views.put(view(recordName).withFields(keywords));
        records.define(recordName, keywords.toArray(Keyword.class));
        Number updated = records.put(recordName, update(using(unique), recordsToAdd));
        return numberOfRecordsUpdated(updated);
    }

    private String numberOfRecordsUpdated(Number updated) {
        return format("%s Records updated", updated);
    }

}
