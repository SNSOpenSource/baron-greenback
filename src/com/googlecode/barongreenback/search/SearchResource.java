package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.googlecode.barongreenback.search.RecordsService.unalias;
import static com.googlecode.barongreenback.search.RecordsService.visibleHeaders;
import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.shared.sorter.Sorter.sortKeywordFromRequest;
import static com.googlecode.barongreenback.views.ViewsRepository.unwrap;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.GenericType.functions.forClass;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.isEmpty;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.time.Dates.LEXICAL;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_CSV;


@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
@Path("{view}/search")
public class SearchResource {
    private final Redirector redirector;
    private final Pager pager;
    private final Sorter sorter;
    private final RecordsService recordsService;
    private final Clock clock;
    private final ShortcutPolicy shortcutPolicy;
    private final CsvWriter csvWriter;

    public SearchResource(final Redirector redirector, final Pager pager, final Sorter sorter, final RecordsService recordsService, final Clock clock, final ShortcutPolicy shortcutPolicy, CsvWriter csvWriter) {
        this.redirector = redirector;
        this.pager = pager;
        this.sorter = sorter;
        this.recordsService = recordsService;
        this.clock = clock;
        this.shortcutPolicy = shortcutPolicy;
        this.csvWriter = csvWriter;
    }

    @GET
    @Priority(Priority.High)
    @Path("list")
    public Model list(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        final Either<String, Sequence<Record>> errorOrResults = recordsService.findFromView(viewName, query);
        return results(viewName, query, errorOrResults);
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
    public Response exportCsv(@PathParam("view") final String viewName, @QueryParam("id") Iterable<String> id) {
        String idName = unwrap(recordsService.view(viewName)).get("keywords", Model.class).get("name", String.class);
        return exportCsv(viewName, sequence(id).map(Strings.format(idName + ":%s")).toString(" OR "));
    }

    @GET
    @Produces(MediaType.TEXT_CSV)
    @Path("csv")
    public Response exportCsv(@PathParam("view") final String viewName, @QueryParam("query") @DefaultValue("") final String query) {
        final Either<String, Sequence<Record>> errorOrResults = recordsService.findFromView(viewName, query);
        final Model view = recordsService.view(viewName);
        final Definition definition = recordsService.definition(view);
        Keyword<? extends Comparable> firstComparable = findFirstComparable(definition);
        final Sequence<Record> result = errorOrResults.right().sortBy(descending(firstComparable)).map(withAliasesFor(recordsService.visibleHeaders(viewName)));

        final Sequence<Keyword<?>> visibleHeaders = visibleHeaders(view);

        return ResponseBuilder.response().
                header(CONTENT_TYPE, TEXT_CSV).
                header("Content-Disposition", String.format("attachment; filename=%s-export-%s.csv", viewName, LEXICAL().format(clock.now()))).
                entity(new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException {
                        using(new OutputStreamWriter(new BufferedOutputStream(outputStream, 32768)), new Block<OutputStreamWriter>() {
                            @Override
                            public void execute(OutputStreamWriter writer) throws Exception {
                                csvWriter.writeTo(result, writer, visibleHeaders);
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
        if (shortcutPolicy.shouldShortcut(viewName, query)) {
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
                return Boolean.TRUE.equals(other.metadata().get(Keywords.unique));
            }
        });
    }

    @GET
    @Path("unique")
    public Object unique(@PathParam("view") String viewName, @QueryParam("query") String query) {
        final Option<Record> record = recordsService.findUnique(viewName, query);
        if (record.isEmpty()) return Responses.response(Status.NOT_FOUND);

        final List<Model> viewFields = recordsService.findView(viewName).get().get("view", Model.class).getValues("keywords", Model.class);
        final List<String> aliasedViewFieldNames = sequence(viewFields).map(toAliasOrElseName()).unique().toList();
        final List<String> viewGroupNames = sequence(viewFields).map(value("group", String.class)).unique().toList();
        final Map<String, Map<String, Object>> groupedAliasedFields = record.map(withAliasesFor(headers(recordsService.view(viewName)))).get().fields().fold(newSortedMapUsing(viewGroupNames), groupBy(ViewsRepository.GROUP, aliasedViewFieldNames));

        return baseModel(viewName, query).add("record", groupedAliasedFields);
    }

    private Mapper<Model, String> toAliasOrElseName() {
        return new Mapper<Model, String>() {
            @Override
            public String call(Model viewField) throws Exception {
                return viewField.getOption("alias", String.class).getOrElse(viewField.get("name", String.class));
            }
        };
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

                Sequence<Record> results = pager.paginate(sorter.sort(unpaged, sortKeywordFromRequest(RecordsService.visibleHeaders(view.get())).then(unalias())));
                if (results.isEmpty()) return baseModel(viewName, query);

                final Sequence<Keyword<?>> visibleHeaders = recordsService.visibleHeaders(viewName);

                return pager.model(sorter.model(baseModel(viewName, query).
                                add("results", results.map(withAliasesFor(recordsService.visibleHeaders(viewName)).map(asModel(viewName, visibleHeaders))).toList()).
                                add("resultCount", recordsService.count(viewName, query)),
                        visibleHeaders, results
                ));
            }
        };
    }

    private UnaryFunction<Record> withAliasesFor(Sequence<Keyword<?>> keywords) {
        final Sequence<AliasedKeyword<?>> aliasedKeywords = keywords.safeCast(AliasedKeyword.class).unsafeCast();
        final Sequence<Keyword<?>> sourceKeywords = aliasedKeywords.map(unalias());
        return alias(aliasedKeywords, sourceKeywords);
    }

    private UnaryFunction<Record> alias(final Sequence<AliasedKeyword<?>> aliasedKeywords, final Sequence<Keyword<?>> sourceKeywords) {
        return new UnaryFunction<Record>() {
            @Override
            public Record call(Record record) throws Exception {
                final Pair<Sequence<Pair<Keyword<?>, Object>>, Sequence<Pair<Keyword<?>, Object>>> partition = record.fields().partition(where(Callables.<Keyword<?>>first(), in(sourceKeywords)));
                return record(partition.first().map(Callables.<Keyword<?>, Object, Keyword<?>>first(new UnaryFunction<Keyword<?>>() {
                    @Override
                    public Keyword<?> call(Keyword<?> keyword) throws Exception {
                        return aliasedKeywords.filter(where(unalias(), Predicates.<Keyword<?>>is(keyword))).head();
                    }
                })).join(partition.second()));
            }
        };
    }

    private Callable1<? super Record, Model> asModel(final String viewName, final Sequence<Keyword<?>> visibleHeaders) {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                Sequence<Keyword<?>> headers = visibleHeaders.isEmpty() ? record.keywords() : visibleHeaders;
                Model model = model();
                for (Keyword<?> header : headers) {
                    Model field = model().
                            add("value", record.get(header));

                    if (Boolean.TRUE.equals(header.metadata().get(Keywords.unique))) {
                        field.add("url", uniqueUrlOf(record, header, viewName));
                    }

                    model.add(header.name(), field);
                }
                return model;
            }
        };
    }

    private Uri uniqueUrlOf(Record record, Keyword<?> visibleHeader, String viewName) {
        return redirector.uriOf(method(on(SearchResource.class).
                unique(viewName, String.format("%s:\"%s\"", visibleHeader.name(), record.get(visibleHeader))))).
                dropScheme().dropAuthority();
    }

    public Callable2<Map<String, Map<String, Object>>, Pair<Keyword<?>, Object>, Map<String, Map<String, Object>>> groupBy(final Keyword<String> lookupKeyword, final List<String> fieldNames) {
        return new Callable2<Map<String, Map<String, Object>>, Pair<Keyword<?>, Object>, Map<String, Map<String, Object>>>() {
            public Map<String, Map<String, Object>> call(Map<String, Map<String, Object>> map, Pair<Keyword<?>, Object> pair) throws Exception {
                Keyword<?> keyword = pair.first();
                Object fieldValue = pair.second();
                String groupName = keyword.metadata().get(lookupKeyword);
                if (isEmpty(groupName)) groupName = "Other";
                if (!map.containsKey(groupName)) {
                    map.put(groupName, new TreeMap(fixedOrderStringComparator(fieldNames)));
                }
                map.get(groupName).put(keyword.name(), fieldValue);
                return map;
            }
        };
    }

    private Sequence<Keyword<?>> headers(Model view) {
        return toKeywords(unwrap(view));
    }

    private Map<String, Map<String, Object>> newSortedMapUsing(final List<String> expectedOrder) {
        return new TreeMap<String, Map<String, Object>>(fixedOrderStringComparator(expectedOrder));
    }

    private Comparator<String> fixedOrderStringComparator(final List<String> expectedOrder) {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                final int indexOfO1 = expectedOrder.indexOf(o1);
                final int indexOfO2 = expectedOrder.indexOf(o2);
                if (indexOfO1 < 0 && indexOfO2 < 0) return o1.compareTo(o2);
                if (indexOfO1 < 0) return 1;
                if (indexOfO2 < 0) return -1;
                return indexOfO1 - indexOfO2;
            }
        };
    }
}
