package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
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

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.barongreenback.views.View.view;
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
    private final Records records;
    private final Crawler crawler;
    private final Views views;

    public CrawlerResource(Records records, Crawler crawler, Views views) {
        this.records = records;
        this.crawler = crawler;
        this.views = views;
    }

    @GET
    @Path("new")
    public Model get(@QueryParam("numberOfFields") @DefaultValue("10") Integer numberOfFields) {
        return Model.model().add("numberOfFields", new int[Math.min(Math.max(numberOfFields,1), 100)]);
    }

    @POST
    @Path("new")
    public Response crawl(@FormParam("from") URL from, @FormParam("update") String update, RecordDefinition recordDefinition
    ) throws Exception {
        Sequence<Record> extractedValues = crawler.crawl(from, recordDefinition);
        return put(keyword(update), uniqueFields(recordDefinition), extractedValues);
    }

    private Response put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        views.put(view(recordName).withFields(keywords(recordsToAdd)));
        records.put(recordName, update(using(unique), recordsToAdd));
        return redirect(resource(SearchResource.class).find(recordName.name(), EMPTY));
    }
}
