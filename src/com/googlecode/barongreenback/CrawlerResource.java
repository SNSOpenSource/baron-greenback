package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
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
import org.apache.lucene.queryParser.ParseException;

import java.net.URL;

import static com.googlecode.barongreenback.View.view;
import static com.googlecode.barongreenback.XmlDefinition.uniqueFields;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("admin/crawl")
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
    public Model get() {
        return Model.model();
    }

    @POST
    public Response crawl(@FormParam("from") URL from, @FormParam("update") String update, XmlDefinition xmlDefinition
    ) throws Exception {
        Sequence<Record> extractedValues = crawler.crawl(from, xmlDefinition);
        return put(keyword(update), uniqueFields(xmlDefinition), extractedValues);
    }

    private Response put(final Keyword<Object> recordName, Sequence<Keyword> unique, final Sequence<Record> recordsToAdd) throws ParseException {
        views.add(view(recordName).withFields(com.googlecode.barongreenback.Callables.keywords(recordsToAdd)));
        records.put(recordName, update(using(unique), recordsToAdd));
        return redirect(resource(SearchResource.class).find(recordName.name(), EMPTY));
    }
}
