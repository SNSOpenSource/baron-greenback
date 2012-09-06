package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.AdvancedMode;
import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.CsvWriter;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Responses;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.StreamingOutput;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Priority;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.barongreenback.search.RecordsService.visibleHeaders;
import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.views.ViewsRepository.unwrap;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.GenericType.functions.forClass;
import static com.googlecode.totallylazy.Predicates.classAssignableTo;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.time.Dates.LEXICAL;
import static com.googlecode.totallylazy.time.Dates.LUCENE;


@Produces(MediaType.TEXT_HTML)
@Path("{view}/search")
public class SearchResource {
    private final Redirector redirector;
    private final Pager pager;
    private final Sorter sorter;
    private final RecordsService recordsService;
    private final Clock clock;

    public SearchResource(final Redirector redirector, final Pager pager, final Sorter sorter, final RecordsService recordsService, final Clock clock) {
        this.redirector = redirector;
        this.pager = pager;
        this.sorter = sorter;
        this.recordsService = recordsService;
        this.clock = clock;
    }

    @GET
    @Priority(Priority.High)
    @Path("list")
    public Model list(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        final Either<String, Sequence<Record>> errorOrResults = recordsService.findFromView(viewName, query);
        return results(viewName, query, errorOrResults);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    public String listJson(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        return list(viewName, query).toString();
    }

    @GET
    @Path("all")
    public Model all(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        final Either<String, Sequence<Record>> errorOrResults = recordsService.findAll(viewName, query);
        return results(viewName, query, errorOrResults);
    }

    @GET
    @Produces(MediaType.TEXT_CSV)
    @Path("csv")
    public Response exportCsv(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        final Either<String, Sequence<Record>> errorOrResults = recordsService.findFromView(viewName, query);
        final Model view = recordsService.view(viewName);
        final Definition definition = recordsService.definition(view);
        Keyword<? extends Comparable> firstComparable = findFirstComparable(definition);
        final Iterator<Record> result = errorOrResults.right().sortBy(descending(firstComparable)).iterator();

        final Sequence<Keyword<?>> visibleHeaders = visibleHeaders(view);

        return ResponseBuilder.response().
                header("Content-Disposition", String.format("filename=%s-export-%s.csv", viewName, LEXICAL().format(clock.now()))).
                entity(new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException {
                        using(new OutputStreamWriter(new BufferedOutputStream(outputStream, 32768)), new Function1<OutputStreamWriter, Void>() {
                            @Override
                            public Void call(OutputStreamWriter writer) throws Exception {
                                CsvWriter.writeTo(result, writer, visibleHeaders);
                                return Runnables.VOID;
                            }
                        });
                    }
                })
                .build();
    }

    private Model results(String viewName, String query, Either<String, Sequence<Record>> errorOrResults) {
        return errorOrResults.map(handleError(viewName, query), listResults(viewName, query));
    }

    private Keyword<? extends Comparable> findFirstComparable(Definition definition) {
        return cast(definition.fields().find(Predicates.where(forClass(), classAssignableTo(Comparable.class))).get());
    }

    @GET
    @Path("shortcut")
    public Object shortcut(@PathParam("view") final String viewName, @QueryParam("query") final String query) {
        if (recordsService.count(viewName, query).intValue() == 1) {
            final Sequence<Keyword<?>> visibleHeaders = recordsService.visibleHeaders(viewName);
            final Option<Record> optionalRecord = recordsService.findUnique(viewName, query);
            Option<Keyword<?>> unique = uniqueHeader(visibleHeaders);
            return Responses.seeOther(uniqueUrlOf(optionalRecord.get(), unique.get(), viewName));
        } else {
            return Responses.seeOther(redirector.uriOf(method(on(this.getClass()).list(viewName, query))));
        }
    }

    private Option<Keyword<?>> uniqueHeader(Sequence<Keyword<?>> visibleHeaders) {
        return visibleHeaders.find(new Predicate<Keyword<?>>() {
            @Override
            public boolean matches(Keyword<?> other) {
                return Boolean.TRUE.equals(other.metadata().get(Keywords.UNIQUE));
            }
        });
    }

    @GET
    @Path("unique")
    public Object unique(@PathParam("view") String viewName, @QueryParam("query") String query) {
        final Option<Record> record = recordsService.findUnique(viewName, query);
        if (record.isEmpty()) return Responses.response(Status.NOT_FOUND);

        Map<String, Map<String, Object>> group = record.get().fields().fold(new LinkedHashMap<String, Map<String, Object>>(), groupBy(ViewsRepository.GROUP));
        return baseModel(viewName, query).add("record", group);
    }

    private Model baseModel(String viewName, String query) {
        return model().add("view", viewName).add("query", query);
    }

    private Callable1<String, Model> handleError(final String viewName, final String query) {
        return new Callable1<String, Model>() {
            @Override
            public Model call(String errorMessage) throws Exception {
                return baseModel(viewName, query).add("queryException", errorMessage);
            }
        };
    }

    private Callable1<Sequence<Record>, Model> listResults(final String viewName, final String query) {
        return new Callable1<Sequence<Record>, Model>() {
            @Override
            public Model call(Sequence<Record> unpaged) throws Exception {
                Option<Model> view = recordsService.findView(viewName);
                if (view.isEmpty()) return baseModel(viewName, query);

                Sequence<Record> results = pager.paginate(sorter.sort(unpaged, headers(view.get())));
                if (results.isEmpty()) return baseModel(viewName, query);

                final Sequence<Keyword<?>> visibleHeaders = recordsService.visibleHeaders(viewName);
                return pager.model(sorter.model(baseModel(viewName, query).
                        add("results", results.map(asModel(viewName, visibleHeaders)).toList()),
                        visibleHeaders, results));
            }
        };
    }

    private Callable1<? super Record, Model> asModel(final String viewName, final Sequence<Keyword<?>> visibleHeaders) {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                Sequence<Keyword<?>> headers = visibleHeaders.isEmpty() ? record.keywords() : visibleHeaders;
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

    private Uri uniqueUrlOf(Record record, Keyword visibleHeader, String viewName) {
        return redirector.uriOf(method(on(SearchResource.class).
                unique(viewName, String.format("%s:\"%s\"", visibleHeader.name(), record.get(visibleHeader))))).
                dropScheme().dropAuthority();
    }

    public static Callable2<Map<String, Map<String, Object>>, Pair<Keyword<?>, Object>, Map<String, Map<String, Object>>> groupBy(final Keyword<String> lookupKeyword) {
        return new Callable2<Map<String, Map<String, Object>>, Pair<Keyword<?>, Object>, Map<String, Map<String, Object>>>() {
            public Map<String, Map<String, Object>> call(Map<String, Map<String, Object>> map, Pair<Keyword<?>, Object> pair) throws Exception {
                Keyword<?> keyword = pair.first();
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

    private Sequence<Keyword<?>> headers(Model view) {
        return toKeywords(unwrap(view));
    }
}
