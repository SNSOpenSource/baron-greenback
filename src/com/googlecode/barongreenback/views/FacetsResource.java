package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.search.DrillDowns;
import com.googlecode.barongreenback.search.PredicateBuilder;
import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetDrillDown;
import com.googlecode.lazyrecords.FacetRequest;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.googlecode.barongreenback.search.RecordsService.headers;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.funclate.Model.methods.merge;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.lazyrecords.FacetRequest.facetRequest;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.totallylazy.Callables.returns1;
import static com.googlecode.totallylazy.Maps.entries;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;

@Produces({MediaType.TEXT_HTML})
@Path("facets")
public class FacetsResource {
    private static final String INVALID_DRILLS_MESSAGE = "Drill downs can't be parsed";

    private final FacetedRecords records;
    private final PredicateBuilder predicateBuilder;
    private final ModelRepository modelRepository;
    private final StringMappings stringMappings;
    private final Redirector redirector;

    public FacetsResource(FacetedRecords records,
                          PredicateBuilder predicateBuilder,
                          ModelRepository modelRepository,
                          BaronGreenbackStringMappings baronGreenbackStringMappings, Redirector redirector) {
        this.records = records;
        this.predicateBuilder = predicateBuilder;
        this.modelRepository = modelRepository;
        this.redirector = redirector;
        this.stringMappings = baronGreenbackStringMappings.value();
    }

    @GET
    @Path("facets")
    public Model list(@QueryParam("current") @DefaultValue("") String currentView, @QueryParam("query") @DefaultValue("") String query, @QueryParam("drills") Either<String, DrillDowns> drillDowns) throws IOException {
        final Model drillDownsExceptionModel = drillDowns.map(returns1(drillDownExceptionModel(INVALID_DRILLS_MESSAGE)), returns1(model()));

        final Option<Model> viewOption = ViewsRepository.find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return merge(model()
                    .add("facets", empty())
                    .add("query", query)
                    .add("view", currentView)
                    .add("drills", drillDowns.map(Callables.asString(), Callables.asString())), drillDownsExceptionModel);
        }

        final Sequence<Keyword<?>> viewHeaders = headers(viewOption.get()).map(RecordsService.unalias());
        final Either<String, Predicate<Record>> queryPredicate = predicateBuilder.build(query, viewHeaders, DrillDowns.empty());

        final Map<Keyword<?>, Integer> keywordAndFacetEntries = Maps.map(viewHeaders.filter(where(metadata(ViewsRepository.SHOW_FACET), is(notNullValue(Boolean.class).and(is(true)))))
                .map(toKeywordAndFacetEntries()));
        final Sequence<FacetRequest> facetRequests = entries(keywordAndFacetEntries).map(asFacetRequest());
        final Sequence<FacetDrillDown> facetDrillDowns = calculateDrillDowns(drillDowns.rightOption().getOrElse(DrillDowns.empty()), viewHeaders);
        final Sequence<Facet<FacetEntry>> facets = records.facetResults(queryPredicate.right(), facetRequests, facetDrillDowns);
        return merge(model()
                .add("facets", facets.map(asFacetModel(keywordAndFacetEntries, facetDrillDowns, currentView, query, drillDowns)))
                .add("query", query)
                .add("view", currentView)
                .add("drills", drillDowns.map(Callables.asString(), Callables.asString())), drillDownsExceptionModel);
    }

    private Model drillDownExceptionModel(String message) {
        return model().add("drillDownsException", message);
    }

    @GET
    @Path("facet")
    public Model facet(@QueryParam("current") @DefaultValue("") String currentView, @QueryParam("query") @DefaultValue("") String query, @QueryParam("facetName") String facetName, @QueryParam("entryCount") Option<Integer> entryCount, @QueryParam("drills") Either<String, DrillDowns> drillDowns) throws IOException {
        final Model drillDownsExceptionModel = drillDowns.map(returns1(drillDownExceptionModel(INVALID_DRILLS_MESSAGE)), returns1(model()));

        final Option<Model> viewOption = ViewsRepository.find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return merge(model().add("entries", empty()), drillDownsExceptionModel);
        }

        final Option<Keyword<?>> viewHeader = headers(viewOption.get()).find(where(name(), is(facetName)));
        final Keyword<?> facetKeyword = viewHeader.getOrThrow(new RuntimeException("No facet found with name " + facetName));
        final Either<String, Predicate<Record>> queryPredicate = predicateBuilder.build(query, Sequences.<Keyword<?>>one(facetKeyword), DrillDowns.empty());
        final Sequence<FacetDrillDown> facetDrillDowns = calculateDrillDowns(drillDowns.right(), Sequences.<Keyword<?>>one(facetKeyword));

        final Facet<FacetEntry> facet = records.facetResults(queryPredicate.right(), one(facetRequest(facetKeyword)), facetDrillDowns).first();
        final int totalSize = facet.size();
        final int viewDefinitionConfiguredSize = facetKeyword.metadata().get(ViewsRepository.FACET_ENTRIES).intValue();
        final Integer displayedSize = Math.min(totalSize, entryCount.getOrElse(viewDefinitionConfiguredSize));

        Model facetModel = model().
                add("name", facetName).
                add("class-name", classNameOf(facetName)).
                add("entries", facet.take(displayedSize).map(asFacetEntryModel(facetKeyword, facetDrillDowns)));

        if (totalSize > displayedSize) {
            facetModel = facetModel.add("more", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facet.key().name(), Option.some(Integer.MAX_VALUE), drillDowns))).toString());
        }
        if (displayedSize == totalSize && displayedSize > viewDefinitionConfiguredSize) {
            facetModel = facetModel.add("fewer", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facet.key().name(), Option.none(Integer.class), drillDowns))).toString());
        }
        return merge(
                model().
                        add("facet", facetModel).
                        add("drills", drillDowns.map(Callables.asString(), Callables.asString())),
                drillDownsExceptionModel);
    }

    private String classNameOf(String name) {
        return name.replaceAll(" ", "-");
    }

    private Sequence<FacetDrillDown> calculateDrillDowns(DrillDowns drillDowns, final Sequence<Keyword<?>> viewHeaders) {
        return pairs(drillDowns.value()).map(toFacetDrillDown(viewHeaders));
    }

    private Mapper<Pair<String, List<String>>, FacetDrillDown> toFacetDrillDown(final Sequence<Keyword<?>> viewHeaders) {
        return new Mapper<Pair<String, List<String>>, FacetDrillDown>() {
            @Override
            public FacetDrillDown call(Pair<String, List<String>> facetDrillDown) throws Exception {
                final Keyword<?> facetKeyword = viewHeaders.find(where(name(), is(facetDrillDown.first()))).get();
                return FacetDrillDown.facetDrillDown(facetKeyword, facetDrillDown.second());
            }
        };
    }


    private Mapper<Map.Entry<Keyword<?>, Integer>, FacetRequest> asFacetRequest() {
        return new Mapper<Map.Entry<Keyword<?>, Integer>, FacetRequest>() {
            @Override
            public FacetRequest call(Map.Entry<Keyword<?>, Integer> keywordAndEntries) throws Exception {
                return facetRequest(keywordAndEntries.getKey());
            }
        };
    }

    private Mapper<Keyword<?>, Pair<Keyword<?>, Integer>> toKeywordAndFacetEntries() {
        return new Mapper<Keyword<?>, Pair<Keyword<?>, Integer>>() {

            @Override
            public Pair<Keyword<?>, Integer> call(Keyword<?> header) throws Exception {
                final int facetEntries = header.metadata().get(ViewsRepository.FACET_ENTRIES).intValue();
                return Pair.<Keyword<?>, Integer>pair(header, facetEntries);
            }
        };
    }

    private Mapper<Facet<FacetEntry>, Model> asFacetModel(final Map<Keyword<?>, Integer> keywordAndFacetEntries, final Sequence<FacetDrillDown> facetDrillDowns, final String currentView, final String query, final Either<String, DrillDowns> drillDowns) throws IOException {
        return new Mapper<Facet<FacetEntry>, Model>() {
            @Override
            public Model call(Facet<FacetEntry> facet) throws Exception {
                final Keyword<?> facetKeyword = facet.key();
                final int totalSize = facet.size();
                final Sequence<Model> facetEntries = facet.map(asFacetEntryModel(facetKeyword, facetDrillDowns));
                final int configuredSize = keywordAndFacetEntries.get(facetKeyword);
                final int maxDrilledDownIndex = facetEntries.map(value("drilledDown", Boolean.class)).toList().lastIndexOf(Boolean.TRUE);
                final int entriesToDisplay = Math.max(configuredSize, maxDrilledDownIndex + 1);
                final Model base = model().
                        add("name", facet.key().name()).
                        add("class-name", classNameOf(facet.key().name())).
                        add("entries", facetEntries.take(entriesToDisplay));
                if (totalSize > entriesToDisplay) {
                    return base.add("more", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facet.key().name(), Option.some(Integer.MAX_VALUE), drillDowns))).toString());
                }
                return base;
            }
        };
    }

    private Mapper<FacetEntry, Model> asFacetEntryModel(final Keyword<?> facetKeyword, final Sequence<FacetDrillDown> facetDrillDowns) {
        return new Mapper<FacetEntry, Model>() {
            @Override
            public Model call(final FacetEntry facetEntry) throws Exception {
                boolean drilledDown = facetDrillDowns.exists(isDrilledDown(facetKeyword, facetEntry));
                return model()
                        .add("value", stringMappings.toString(facetKeyword.forClass(), facetEntry.first()))
                        .add("displayValue", facetEntry.first())
                        .add("itemsTotal", facetEntry.second())
                        .add("drilledDown", drilledDown);
            }
        };
    }

    private Predicate<FacetDrillDown> isDrilledDown(final Keyword<?> facetKeyword, final FacetEntry facetEntry) {
        return new Predicate<FacetDrillDown>() {
            @Override
            public boolean matches(FacetDrillDown facet) {
                if (facet.key().equals(facetKeyword)) {
                    return facet.contains(stringMappings.toString(facetKeyword.forClass(), facetEntry.first()));
                }
                return false;
            }
        };
    }

}
