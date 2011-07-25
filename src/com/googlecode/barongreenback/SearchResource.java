package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import com.googlecode.utterlyidle.rendering.Model;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import static com.googlecode.utterlyidle.rendering.Model.model;


@Path("search")
@Produces(MediaType.TEXT_HTML)
public class SearchResource {
    private final LuceneRecords records;
    private final QueryParser parser;

    public SearchResource(final LuceneRecords records, final QueryParser parser) {
        this.records = records;
        this.parser = parser;
    }

    @GET
    public Model find(@QueryParam("query") String query) throws ParseException {
        Sequence<Record> results = records.query(parser.parse(query));
        return model().
                add("query", query).
                add("results", results);
    }
}
