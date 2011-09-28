package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.views.View;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.lucene.Lucene.and;


@Produces(MediaType.TEXT_HTML)
@Path("{view}/search")
public class SearchResource {
    private final LuceneRecords records;
    private final QueryParserActivator parser;
    private final Views views;

    public SearchResource(final LuceneRecords records, final QueryParserActivator parser, final Views views) {
        this.records = records;
        this.parser = parser;
        this.views = views;
    }

    @GET
    @Path("list")
    public Model list(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Sequence<Keyword> allHeaders = headers(view);
        Sequence<Keyword> visibleHeaders = visibleHeaders(allHeaders);
        Sequence<Record> results = records.query(and(type(view), parse(query, visibleHeaders)), allHeaders);
        return model().
                add("view", view).
                add("query", query).
                add("headers", headers(visibleHeaders, results)).
                add("results", results.map(RecordMethods.asMap()).toList());
    }

    private TermQuery type(String view) {
        return new TermQuery(new Term(Lucene.RECORD_KEY.name(), view));
    }

    @GET
    @Path("unique")
    public Model unique(@PathParam("view") String view, @QueryParam("query") String query) throws ParseException {
        Sequence<Keyword> headers = headers(view);
        Record record = records.query(and(type(view), parse(query, headers)), headers).head();
        Map<String, Map<String, Object>> fold = record.fields().fold(new LinkedHashMap<String, Map<String, Object>>(), groupBy(Views.GROUP));
        return model().
                add("view", view).
                add("record", fold);
    }

    public static Callable2<Map<String, Map<String, Object>>, Pair<Keyword, Object>, Map<String, Map<String, Object>>> groupBy(final Keyword<String> lookupKeyword) {
        return new Callable2<Map<String, Map<String, Object>>, Pair<Keyword, Object>, Map<String, Map<String, Object>>>() {
            public Map<String, Map<String, Object>> call(Map<String, Map<String, Object>> map, Pair<Keyword, Object> pair) throws Exception {
                Keyword keyword = pair.first();
                Object value = pair.second();
                String key = keyword.metadata().get(lookupKeyword);
                if(key.isEmpty()) key = "Other";
                if(!map.containsKey(key)){
                    map.put(key, new LinkedHashMap<String, Object>());
                }
                map.get(key).put(keyword.name(), value);
                return map;
            }
        };
    }

    private Sequence<Keyword> headers(String view) {
        Option<View> optionalView = views.get(view);
        return optionalView.map(View.asFields()).getOrElse(Sequences.<Keyword>empty());
    }

    private List<Map<String, Object>> headers(Sequence<Keyword> headers, Sequence<Record> results) {
        if(headers.isEmpty()){
            return toModel(keywords(results));
        }
        return toModel(headers);
    }

    private Sequence<Keyword> visibleHeaders(Sequence<Keyword> headers) {
        return headers.filter(where(metadata(Views.VISIBLE), is(notNullValue(Boolean.class).and(is(true)))));
    }

    private List<Map<String, Object>> toModel(Sequence<Keyword> keywords) {
        return keywords.map(asHeader()).
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

    private Query parse(String query, Sequence<Keyword> keywords) throws ParseException {
        if (query.isEmpty()) {
            return new MatchAllDocsQuery();
        }
        return parser.create(keywords.map(asString()).toArray(String.class)).parse(query);
    }

}
