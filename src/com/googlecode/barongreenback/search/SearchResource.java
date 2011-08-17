package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.views.View;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.RecordMethods;
import com.googlecode.totallylazy.records.lucene.Lucene;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import java.util.List;
import java.util.Map;

import static com.googlecode.barongreenback.views.View.asFields;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.RecordMethods.toMap;


@Produces(MediaType.TEXT_HTML)
@Path("{view}/search")
public class SearchResource {
    private final LuceneRecords records;
    private final QueryParser parser;
    private final Views views;

    public SearchResource(final LuceneRecords records, final QueryParser parser, final Views views) {
        this.records = records;
        this.parser = parser;
        this.views = views;
    }

    @GET
    @Path("list")
    public Model find(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Option<View> optionalView = views.get(view);
        Sequence<Record> results = records.query(parse(prefix(view, query)), optionalView.map(View.asFields()).getOrElse(Sequences.<Keyword>empty()));
        return model().
                add("view", view).
                add("query", query).
                add("headers", headers(optionalView, results)).
                add("results", results.map(RecordMethods.asMap()).toList());
    }

    @GET
    @Path("unique")
    public Model unique(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Option<View> optionalView = views.get(view);
        Record record = records.query(parse(prefix(view, query)), optionalView.map(asFields()).getOrElse(Sequences.<Keyword>empty())).head();
        return model().
                add("view", view).
                add("record", toMap(record));
    }

    private List<Map<String, Object>> headers(Option<View> optionalView, Sequence<Record> results) {
        return optionalView.map(asFields()).
                getOrElse(keywords(results)).
                map(asHeader()).
                map(Model.asMap()).
                toList();
    }

    private Callable1<? super Keyword, Model> asHeader() {
        return new Callable1<Keyword, Model>() {
            public Model call(Keyword keyword) throws Exception {
                return model().
                        add("name", keyword.name()).
                        add("unique", keyword.metadata().get(Keywords.UNIQUE));
            }
        };
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
