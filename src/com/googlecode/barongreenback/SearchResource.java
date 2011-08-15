package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
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

import static com.googlecode.barongreenback.Callables.asMap;
import static com.googlecode.barongreenback.Callables.keywords;
import static com.googlecode.barongreenback.Callables.toMap;
import static com.googlecode.funclate.Model.model;


@Produces(MediaType.TEXT_HTML)
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
    @Path("{view}/search")
    public Model find(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Option<View> optionalView = views.get(view);
        Sequence<Record> results = records.query(parse(prefix(view, query)), Sequences.<Keyword>empty());
        return model().
                add("view", view).
                add("query", query).
                add("headers", headers(optionalView, results)).
                add("results", results.map(asMap()).toList());
    }

    @GET
    @Path("{view}/unique")
    public Model unique(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Record record = records.query(parse(prefix(view, query)), Sequences.<Keyword>empty()).head();
        return model().
                add("view", view).
                add("record", toMap(record));
    }

    private List<Map<String, Object>> headers(Option<View> optionalView, Sequence<Record> results) {
        return optionalView.map(fieldNames()).
                getOrElse(keywords(results)).
                map(asHeader()).
                map(modelAsMap()).
                toList();
    }

    private Callable1<? super Model, Map<String, Object>> modelAsMap() {
        return new Callable1<Model, Map<String, Object>>() {
            public Map<String, Object> call(Model model) throws Exception {
                return model.toMap();
            }
        };
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

    private Callable1<? super View,Sequence<Keyword>> fieldNames() {
        return new Callable1<View, Sequence<Keyword>>() {
            public Sequence<Keyword> call(View view) throws Exception {
                return view.getFields();
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
