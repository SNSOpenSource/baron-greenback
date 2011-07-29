package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import static com.googlecode.barongreenback.Callables.asMap;
import static com.googlecode.barongreenback.Callables.headers;
import static com.googlecode.funclate.Model.model;


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
        Sequence<Record> results = records.query(parse(query), Sequences.<Keyword>empty());
        return model().
                add("query", query).
                add("headers", headers(results)).
                add("results", results.map(asMap()).toList());
    }

    private Query parse(String query) throws ParseException {
        if(query.isEmpty()){
            return new MatchAllDocsQuery();
        }
        return parser.parse(query);
    }

}
