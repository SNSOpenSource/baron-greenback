package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.shared.RecordDefinitionExtractor;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.AliasedKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
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
import static com.googlecode.barongreenback.shared.RecordDefinitionExtractor.RECORD_NAME;
import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("crawler")
@Produces(MediaType.TEXT_HTML)
public class CrawlerResource {
    public static final Keyword<Object> MODELS = keyword("models");
    public static final Keyword<String> ID = keyword("models_id", String.class);
    public static final Keyword<String> MODEL = keyword("model", String.class);
    private final Records records;
    private final Crawler crawler;
    private final Views views;

    public CrawlerResource(Records records, Crawler crawler, Views views) {
        this.records = records;
        this.crawler = crawler;
        this.views = views;
        records.define(MODELS, ID, MODEL);
    }

    @GET
    @Path("new")
    public Model get(@QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields) {
        return emptyForm(numberOfFields);
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") String id, @QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields) {
        return records.get(MODELS).filter(where(ID, is(id))).map(MODEL).map(asModel()).head();
    }

    private Callable1<? super String, Model> asModel() {
        return new Callable1<String, Model>() {
            public Model call(String value) throws Exception {
                return Model.parse(value);
            }
        };
    }

    private Model emptyForm(Integer numberOfFields) {
        return form("", "", emptyDefinition(numberOfFields(numberOfFields)));
    }

    private int numberOfFields(Integer numberOfFields) {
        return Math.min(Math.max(numberOfFields, 1), 100);
    }

    private Model emptyDefinition(int number) {
        return recordDefinition("", Sequences.repeat(model().add("visible", true)).take(number).toArray(Model.class));
    }

    private Model form(String update, String from, Model definition) {
        return model().
                add("form", model().
                        add("update", update).
                        add("from", from).
                        add("record", definition));
    }

    private Model recordDefinition(String recordName, Model... fields) {
        return model().add(RECORD_NAME, recordName).add("keywords", Sequences.sequence(fields).toList());
    }

    private Model keywordDefinition(String name, String alias, String type, boolean unique, boolean visible, Option<Model> recordDefinition) {
        return model().
                add("name", name).
                add("alias", alias).
                add("type", type).
                add("unique", unique).
                add("visible", visible).
                add("subfeed", !recordDefinition.isEmpty()).
                add("definition", recordDefinition.getOrNull());
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
                          @FormParam("update") String update, @FormParam("from") URL from, RecordDefinition recordDefinition) throws Exception {
        if (action.equals("Save")) {
            String id = UUID.randomUUID().toString();
            records.add(MODELS, record().set(ID, id).set(MODEL, toModel(update, from, recordDefinition).toString()));
            return redirect(resource(getClass()).edit(id, numberOfFields));
        }
        Sequence<Record> extractedValues = crawler.crawl(from, recordDefinition);
        return put(keyword(update), uniqueFields(recordDefinition), extractedValues);
    }

    private Model toModel(String update, URL from, RecordDefinition recordDefinition) {
        return form(update, from.toString(), toModel(recordDefinition));

    }

    private Model toModel(RecordDefinition recordDefinition) {
        return recordDefinition(recordDefinition.recordName().name(),
                recordDefinition.fields().map(new Callable1<Keyword, Model>() {
                    public Model call(Keyword keyword) throws Exception {
                        return keywordDefinition(name(keyword), alias(keyword), type(keyword), unique(keyword), visible(keyword), recordDefinition(keyword));
                    }
                }).toArray(Model.class));
    }

    private Option<Model> recordDefinition(Keyword keyword) {
        return option(keyword.metadata().get(RecordDefinition.RECORD_DEFINITION)).map(new Callable1<RecordDefinition, Model>() {
            public Model call(RecordDefinition recordDefinition) throws Exception {
                return toModel(recordDefinition);
            }
        });
    }

    private boolean visible(Keyword keyword) {
        return booleanValueOf(keyword, Views.VISIBLE);
    }

    private boolean booleanValueOf(Keyword keyword, Keyword<Boolean> metaKeyword) {
        return keyword.metadata().get(metaKeyword) == true;
    }

    private boolean unique(Keyword keyword) {
        return booleanValueOf(keyword, Keywords.UNIQUE);
    }

    private String name(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return ((AliasedKeyword) keyword).source().name();
        }
        return keyword.name();
    }

    private String alias(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return keyword.name();
        }
        return "";
    }

    private String type(Keyword keyword) {
        return keyword.forClass().getName();
    }

    private Response put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        views.put(view(recordName).withFields(keywords(recordsToAdd)));
        records.put(recordName, update(using(unique), recordsToAdd));
        return redirect(resource(SearchResource.class).find(recordName.name(), EMPTY));
    }
}
