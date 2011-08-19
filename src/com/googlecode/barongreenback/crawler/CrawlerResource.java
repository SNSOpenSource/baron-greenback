package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.utterlyidle.FormParameters;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerResource {
    public static final ImmutableKeyword<Object> FORMS = keyword("forms");
    public static final ImmutableKeyword<String> ID = keyword("id", String.class);
    public static final ImmutableKeyword<String> FORM = keyword("form", String.class);
    private final Records records;
    private final Crawler crawler;
    private final Views views;

    public CrawlerResource(Records records, Crawler crawler, Views views) {
        this.records = records;
        this.crawler = crawler;
        this.views = views;
        records.define(FORMS, ID, FORM);
    }

    @GET
    @Path("new")
    public Model get(@QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields) {
        return emptyForm(numberOfFields);
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") String id, @QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields) {
        return form("news", "http://feeds.bbci.co.uk/news/rss.xml",
                recordDefinition("/rss/channel/item",
                        keywordDefinition("title", "", "java.lang.String", false, true, false),
                        keywordDefinition("link", "", "java.net.URI", false, false, true).
                                add("definition", recordDefinition("",
                                        keywordDefinition("status", "", "java.lang.String", false, true, false),
                                        keywordDefinition("id", "", "java.lang.String", true, true, false))),
                        keywordDefinition("guid", "", "java.lang.String", true, true, false)));
    }

    private Model emptyForm(Integer numberOfFields) {
        return form("", "", emptyDefinition(numberOfFields(numberOfFields)));
    }

    private int numberOfFields(Integer numberOfFields) {
        return Math.min(Math.max(numberOfFields, 1), 100);
    }

    private Model emptyDefinition(int number) {
        return recordDefinition("", Sequences.repeat(model()).take(number).toArray(Model.class));
    }

    private Model form(String update, String from, Model definition) {
        return model().
                add("form", model().
                        add("update", update).
                        add("from", from).
                        add("definition", definition));
    }

    private Model recordDefinition(String recordName, Model... fields) {
        return Sequences.sequence(fields).fold(model().add("recordName", recordName), new Callable2<Model, Model, Model>() {
            public Model call(Model model, Model field) throws Exception {
                return model.add("fields", field);
            }
        });
    }

    private Model keywordDefinition(String name, String alias, String type, boolean unique, boolean visible, boolean subfeed) {
        return model().
                add("name", name).
                add("alias", alias).
                add("type", type).
                add("unique", unique).
                add("visible", visible).
                add("subfeed", subfeed);
    }

    private Callable2<? super HashMap<String, List<String>>, ? super Pair<String, String>, HashMap<String, List<String>>> asMultivaluedMap() {
        return new Callable2<HashMap<String, List<String>>, Pair<String, String>, HashMap<String, List<String>>>() {
            public HashMap<String, List<String>> call(HashMap<String, List<String>> map, Pair<String, String> pair) throws Exception {
                if (map.containsKey(pair.first())) {
                    map.get(pair.first()).add(pair.second());
                } else {
                    List<String> values = new ArrayList<String>();
                    values.add(pair.second());
                    map.put(pair.first(), values);
                }
                return map;
            }
        };
    }

    @POST
    @Path("new")
    public Response crawl(@QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields, @FormParam("action") String action,
                          @FormParam("from") URL from, @FormParam("update") String update, RecordDefinition recordDefinition,
                          FormParameters formParameters) throws Exception {
        if (action.equals("Save")) {
            String id = UUID.randomUUID().toString();
            records.add(FORMS, MapRecord.record().set(ID, id).set(FORM, formParameters.toString()));
            return redirect(resource(getClass()).edit(id, numberOfFields));
        }
        Sequence<Record> extractedValues = crawler.crawl(from, recordDefinition);
        return put(keyword(update), uniqueFields(recordDefinition), extractedValues);
    }

    private Response put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        views.put(view(recordName).withFields(keywords(recordsToAdd)));
        records.put(recordName, update(using(unique), recordsToAdd));
        return redirect(resource(SearchResource.class).find(recordName.name(), EMPTY));
    }
}
