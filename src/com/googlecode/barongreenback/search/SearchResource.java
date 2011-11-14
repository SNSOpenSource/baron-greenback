package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.search.parser.PredicateParser;
import com.googlecode.barongreenback.search.parser.StandardParser;
import com.googlecode.barongreenback.shared.AdvancedMode;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.barongreenback.shared.RecordDefinition.asKeywords;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.barongreenback.views.Views.unwrap;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.Keywords.metadata;


@Produces(MediaType.TEXT_HTML)
@Path("{view}/search")
public class SearchResource {
    private final Records records;
    private final ModelRepository modelRepository;
    private final Redirector redirector;
    private final AdvancedMode mode;

    public SearchResource(final Records records, final ModelRepository modelRepository, final Redirector redirector, final AdvancedMode mode) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.redirector = redirector;
        this.mode = mode;
    }

    @POST
    @Path("delete")
    public Response delete(@PathParam("view") String viewName, @QueryParam("query") String query) throws ParseException {
        if(!mode.equals(AdvancedMode.Enable)){
            return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
        }
        Option<Model> optionalView = view(viewName);
        Sequence<Keyword> allHeaders = headers(optionalView);
        Sequence<Keyword> visibleHeaders = visibleHeaders(allHeaders);
        Pair<Keyword, Predicate<Record>> pair = parse(prefix(optionalView, query), visibleHeaders);
        records.remove(pair.first(), pair.second());
        return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
    }

    @GET
    @Path("list")
    public Model list(@PathParam("view") String viewName, @QueryParam("query") final String query) throws ParseException {
        final Option<Model> optionalView = view(viewName);
        Sequence<Keyword> allHeaders = headers(optionalView);
        final Sequence<Keyword> visibleHeaders = visibleHeaders(allHeaders);

        Sequence<Record> results = sequence(optionalView).flatMap(new Callable1<Model, Sequence<Record>>() {
            public Sequence<Record> call(Model model) throws Exception {
                Pair<Keyword, Predicate<Record>> pair = parse(prefix(optionalView, query), visibleHeaders);
                return records.get(pair.first()).filter(pair.second());
            }
        });
        return model().
                add("view", viewName).
                add("query", query).
                add("headers", headers(visibleHeaders, results)).
                add("results", results.map(asModel(viewName, visibleHeaders)).toList());
    }

    @GET
    @Path("unique")
    public Model unique(@PathParam("view") String viewName, @QueryParam("query") String query) throws ParseException {
        Option<Model> optionalView = view(viewName);
        Sequence<Keyword> headers = headers(optionalView);
        Pair<Keyword, Predicate<Record>> pair = parse(prefix(optionalView, query), headers);
        Record record = records.get(pair.first()).filter(pair.second()).head();
        Map<String, Map<String, Object>> fold = record.fields().fold(new LinkedHashMap<String, Map<String, Object>>(), groupBy(Views.GROUP));
        return model().
                add("view", viewName).
                add("query", query).
                add("record", fold);
    }

    private Callable1<? super Record, Model> asModel(final String viewName, final Sequence<Keyword> visibleHeaders) {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                Sequence<Keyword> headers = visibleHeaders.isEmpty() ? record.keywords() : visibleHeaders;
                Model model = model();
                for (Keyword header : headers) {
                    Model field = model().
                            add("value", record.get(header));

                    if (Boolean.TRUE.equals(header.metadata().get(Keywords.UNIQUE))) {
                        field.add("url", uniqueUrlOf(record, header, viewName));
                    }

                    model.add(header.name(), field);
                }
                return model;
            }
        };
    }

    private Uri uniqueUrlOf(Record record, Keyword visibleHeader, String viewName) throws ParseException {
        return redirector.uriOf(method(on(SearchResource.class).
                unique(viewName, String.format("%s:\"%s\"", visibleHeader.name(), record.get(visibleHeader))))).
                dropScheme().dropAuthority();
    }

    private String prefix(Option<Model> optionalView, final String query) {
        return sequence(optionalView.map(query())).add(query).toString(" ");
    }

    private Callable1<Model, String> query() {
        return new Callable1<Model, String>() {
            public String call(Model model) throws Exception {
                return model.get("view", Model.class).get("query", String.class);
            }
        };
    }

    private Option<Model> view(String view) {
        return find(modelRepository, view);
    }

    public static Callable2<Map<String, Map<String, Object>>, Pair<Keyword, Object>, Map<String, Map<String, Object>>> groupBy(final Keyword<String> lookupKeyword) {
        return new Callable2<Map<String, Map<String, Object>>, Pair<Keyword, Object>, Map<String, Map<String, Object>>>() {
            public Map<String, Map<String, Object>> call(Map<String, Map<String, Object>> map, Pair<Keyword, Object> pair) throws Exception {
                Keyword keyword = pair.first();
                Object value = pair.second();
                String key = keyword.metadata().get(lookupKeyword);
                if (Strings.isEmpty(key)) key = "Other";
                if (!map.containsKey(key)) {
                    map.put(key, new LinkedHashMap<String, Object>());
                }
                map.get(key).put(keyword.name(), value);
                return map;
            }
        };
    }

    private Sequence<Keyword> headers(Option<Model> optionalView) {
        return optionalView.
                map(unwrap()).
                map(asKeywords()).
                getOrElse(Sequences.<Keyword>empty());
    }

    private List<Map<String, Object>> headers(Sequence<Keyword> headers, Sequence<Record> results) {
        if (headers.isEmpty()) {
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
                        add("escapedName", escape(keyword.name())).
                        add("unique", keyword.metadata().get(Keywords.UNIQUE));
            }
        };
    }

    private String escape(String name) {
        return name.replace(' ', '_');
    }

    private final Regex extractRecordName = Regex.regex("\\+?type:(\\w+|\".+?\")");
    private Pair<Keyword, Predicate<Record>> parse(String query, Sequence<Keyword> keywords) throws ParseException {
        String recordName = unquote(extractRecordName.match(query).group(1));
        String noRecordName = query.replaceFirst(extractRecordName.toString(), "").trim();
        PredicateParser parser = new StandardParser(keywords);
        Predicate<Record> predicate = parser.parse(noRecordName);
        Keyword keyword = keyword(recordName);
        return Pair.pair(keyword, predicate);
    }

    private String unquote(String possiblyQuoted) {
        return possiblyQuoted.replace("\"", "");
    }

}
