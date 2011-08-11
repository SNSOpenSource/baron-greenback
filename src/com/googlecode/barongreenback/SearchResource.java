package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.Lucene;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import static com.googlecode.barongreenback.Callables.asMap;
import static com.googlecode.barongreenback.Callables.headersAsString;
import static com.googlecode.funclate.Model.model;


@Produces(MediaType.TEXT_HTML)
public class SearchResource {
    private final LuceneRecords records;
    private final QueryParser parser;

    public SearchResource(final LuceneRecords records, final QueryParser parser) {
        this.records = records;
        this.parser = parser;
    }

    @GET
    @Path("{view}/search")
    public Model find(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Sequence<Record> results = records.query(parse(prefix(view, query)), Sequences.<Keyword>empty());
        return model().
                add("view", view).
                add("query", query).
                add("headers", headersAsString(results)).
                add("results", results.map(asMap()).toList());
    }

    private String prefix(String view, String query) {
        if(view.isEmpty()){
            return query;
        }
        return String.format("+%s:%s %s", Lucene.RECORD_KEY, view, query);
    }

    private Query parse(String query) throws ParseException {
        if(query.isEmpty()){
            return new MatchAllDocsQuery();
        }
        return parser.parse(query);
    }

}
