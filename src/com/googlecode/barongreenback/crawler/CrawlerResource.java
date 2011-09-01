package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.ModelRepository.ID;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerResource {
    private final Records records;
    private final ModelRepository modelRepository;
    private final Crawler crawler;
    private final Views views;

    public CrawlerResource(final Records records, final ModelRepository modelRepository, Crawler crawler, Views views) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.crawler = crawler;
        this.views = views;
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
    public Response newCrawler(@FormParam("action") String action, Model model) throws Exception {
        return edit(UUID.randomUUID(), action, model);
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") UUID id) {
        return Forms.addTemplates(modelFor(id));
    }

    @POST
    @Path("edit")
    public Response edit(@QueryParam("id") UUID id, @FormParam("action") String action, final Model root) throws Exception {
        Model form = root.get("form", Model.class);
        String from = form.get("from", String.class);
        String update = form.get("update", String.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        modelRepository.set(id, Forms.form(update, from, recordDefinition.toModel()));
        if (action.equals("Save")) {
            return redirectToList();
        }
        Sequence<Record> extractedValues = crawler.crawl(url(from), recordDefinition);
        return put(keyword(update), uniqueFields(recordDefinition), extractedValues);
    }

    @POST
    @Path("crawl")
    public Response crawl(@FormParam("id") UUID id) throws Exception {
        Model model = modelRepository.get(id);
        Model form = model.get("form", Model.class);
        String from = form.get("from", String.class);
        String update = form.get("update", String.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        Sequence<Record> extractedValues = crawler.crawl(url(from), recordDefinition);
        return put(keyword(update), uniqueFields(recordDefinition), extractedValues);
    }

    private Model modelFor(UUID id) {
        return modelRepository.get(id);
    }

    private Callable1<? super Pair<UUID, Model>, Model> asModelWithId() {
        return new Callable1<Pair<UUID, Model>, Model>() {
            public Model call(Pair<UUID, Model> pair) throws Exception {
                return model().
                        add("id", pair.first().toString()).
                        add("model", pair.second());
            }
        };
    }

    private Response redirectToList() {
        return redirect(resource(getClass()).list());
    }

    private Callable1<? super String, UUID> asUUID() {
        return new Callable1<String, UUID>() {
            public UUID call(String value) throws Exception {
                return UUID.fromString(value);
            }
        };
    }

    private Response put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        views.put(view(recordName).withFields(keywords(recordsToAdd)));
        records.put(recordName, update(using(unique), recordsToAdd));
        return redirect(resource(SearchResource.class).find(recordName.name(), EMPTY));
    }
}
