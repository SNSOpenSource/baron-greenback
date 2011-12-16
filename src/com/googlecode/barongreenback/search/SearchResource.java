package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.search.pager.Pager;
import com.googlecode.barongreenback.search.parser.PredicateParser;
import com.googlecode.barongreenback.search.sorter.Sorter;
import com.googlecode.barongreenback.shared.AdvancedMode;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.barongreenback.views.Views.recordName;
import static com.googlecode.barongreenback.views.Views.unwrap;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.records.Keywords.keywords;
import static com.googlecode.totallylazy.records.Keywords.metadata;


@Produces(MediaType.TEXT_HTML)
@Path("{view}/search")
public class SearchResource {
    private final Records records;
    private final ModelRepository modelRepository;
    private final Redirector redirector;
    private final AdvancedMode mode;
    private final PredicateParser parser;
    private Pager pager;
    private Sorter sorter;

    public SearchResource(final Records records, final ModelRepository modelRepository, final Redirector redirector, final AdvancedMode mode,
                          final PredicateParser parser, final Pager pager, final Sorter sorter) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.redirector = redirector;
        this.mode = mode;
        this.parser = parser;
        this.pager = pager;
        this.sorter = sorter;
    }

    @POST
    @Path("delete")
    public Response delete(@PathParam("view") String viewName, @QueryParam("query") String query) throws ParseException {
        if (!mode.equals(AdvancedMode.Enable)) {
            return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
        }
        Model view = view(viewName);
        Sequence<Keyword> allHeaders = headers(view);
        Sequence<Keyword> visibleHeaders = visibleHeaders(allHeaders);
        Predicate<Record> predicate = parse(prefix(view, query), visibleHeaders).right();
        records.remove(recordName(view), predicate);
        return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
    }

    @GET
    @Path("list")
    public Model list(@PathParam("view") final String viewName, @QueryParam("query") final String query) throws ParseException {
        return optionalView(viewName).
                fold(model().add("view", viewName).add("query", query), executeQuery(viewName, query));
    }

    private Callable2<Model, Model, Model> executeQuery(final String viewName, final String query) {
        return new Callable2<Model, Model, Model>() {
            public Model call(Model result, Model view) throws Exception {
                Sequence<Keyword> allHeaders = headers(view);
                Keyword recordName = recordName(view);
                records.define(recordName, allHeaders.toArray(Keyword.class));
                final Sequence<Keyword> visibleHeaders = visibleHeaders(allHeaders);
                return parse(prefix(view, query), visibleHeaders).
                        map(addQueryException(result), addResults(recordName, allHeaders, result, viewName, visibleHeaders));
            }
        };
    }

    private Callable1<Predicate<Record>, Model> addResults(final Keyword recordName, final Sequence<Keyword> allHeaders, final Model model, final String viewName, final Sequence<Keyword> visibleHeaders) {
        return new Callable1<Predicate<Record>, Model>() {
            public Model call(Predicate<Record> predicate) throws Exception {
                Sequence<Record> results = records.get(recordName).filter(predicate);
                return model.
                        add("headers", headers(visibleHeaders, results)).
                        add("pager", pager).
                        add("sorter", sorter).
                        add("sortLinks", sorter.sortLinks(visibleHeaders)).
                        add("sortedHeaders", sorter.sortedHeaders(visibleHeaders)).
                        add("results", pager.paginate(sorter.sort(results, allHeaders)).map(asModel(viewName, visibleHeaders)).toList());
            }
        };
    }

    private Callable1<? super String, Model> addQueryException(final Model model) {
        return new Callable1<String, Model>() {
            public Model call(String value) throws Exception {
                return model.add("queryException", value);
            }
        };
    }

    @GET
    @Path("unique")
    public Model unique(@PathParam("view") String viewName, @QueryParam("query") String query) throws ParseException {
        Model view = view(viewName);
        Keyword recordName = Views.recordName(view);
        Sequence<Keyword> headers = headers(view);
        Predicate<Record> predicate = parse(prefix(view, query), headers).right();
        records.define(recordName, headers.toArray(Keyword.class));
        Record record = records.get(recordName).filter(predicate).head();
        Map<String, Map<String, Object>> group = record.fields().fold(new LinkedHashMap<String, Map<String, Object>>(), groupBy(Views.GROUP));
        return model().
                add("view", viewName).
                add("query", query).
                add("record", group);
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

    private static String prefix(Model view, final String query) {
        return sequence(queryFrom(view)).add(query).toString(" ");
    }

    private static String queryFrom(Model model) {
        return model.get("view", Model.class).get("query", String.class);
    }

    private Model view(String view) {
        return optionalView(view).get();
    }

    private Option<Model> optionalView(String view) {
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

    private Sequence<Keyword> headers(Model view) {
        return toKeywords(unwrap(view));
    }

    private List<Map<String, Object>> headers(Sequence<Keyword> headers, Sequence<Record> results) {
        if (headers.isEmpty()) {
            return toModel(keywords(results).realise());
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

    private Either<String, Predicate<Record>> parse(String query, Sequence<Keyword> keywords) throws ParseException {
        try {
            Predicate<Record> predicate = parser.parse(query, keywords);
            return Either.right(predicate);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private String unquote(String possiblyQuoted) {
        return possiblyQuoted.replace("\"", "");
    }

}
