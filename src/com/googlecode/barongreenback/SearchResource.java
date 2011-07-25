package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.asString;


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
    public Model find(@QueryParam("query") @DefaultValue("type:users") String query) throws ParseException {
        Sequence<Record> results = records.query(parser.parse(query));
        return model().
                add("query", query).
                add("headers", results.head().keywords().map(asString()).toList()).
                add("results", results.map(asMap()).toList());
    }

    public static Callable1<? super Record, Map> asMap() {
        return new Callable1<Record, Map>() {
            public Map call(Record record) throws Exception {
                return record.fields().fold(new HashMap(), intoMap());
            }
        };
    }

    public static Callable2<? super Map, ? super Pair<Keyword, Object>, Map> intoMap() {
        return new Callable2<Map, Pair<Keyword, Object>, Map>() {
            public Map call(Map map, Pair<Keyword, Object> pair) throws Exception {
                map.put(pair.first().toString(), pair.second());
                return map;
            }
        };
    }
}
