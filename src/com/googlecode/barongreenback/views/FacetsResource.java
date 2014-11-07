package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.search.DrillDowns;
import com.googlecode.barongreenback.search.PredicateBuilder;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetDrillDown;
import com.googlecode.lazyrecords.FacetRequest;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.googlecode.barongreenback.search.RecordsService.headers;
import static com.googlecode.barongreenback.search.RecordsService.unalias;
import static com.googlecode.barongreenback.views.ViewsRepository.FACET_ENTRIES;
import static com.googlecode.barongreenback.views.ViewsRepository.SHOW_FACET;
import static com.googlecode.barongreenback.views.ViewsRepository.find;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.lazyrecords.FacetDrillDown.facetDrillDown;
import static com.googlecode.lazyrecords.FacetRequest.facetRequest;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Callables.returns1;
import static com.googlecode.totallylazy.Maps.entries;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

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

        final Option<Model> viewOption = find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return facetsResponseModel(Sequences.<Model>empty(), currentView, query, drillDowns).merge(drillDownsExceptionModel);
        }

        final Sequence<Keyword<?>> viewHeaders = headers(viewOption.get()).map(unalias());
        final Either<String, Predicate<Record>> queryPredicate = predicateBuilder.build(query, viewHeaders);

        final Map<Keyword<?>, Integer> keywordAndFacetEntries = Maps.map(viewHeaders.filter(where(metadata(SHOW_FACET), is(notNullValue(Boolean.class).and(is(true)))))
                .map(ViewsRepository.toKeywordAndFacetEntries()));
        final Sequence<FacetRequest> facetRequests = entries(keywordAndFacetEntries).map(asFacetRequest());
        final Sequence<FacetDrillDown> facetDrillDowns = calculateDrillDowns(drillDowns.rightOption().getOrElse(DrillDowns.empty()), viewHeaders);
        final Sequence<Facet<FacetEntry>> facets = records.facetResults(queryPredicate.right(), facetRequests, facetDrillDowns);
        final Sequence<Model> facetsModels = facets.map(toSortedEntriesModels(facetDrillDowns)).map(toFacetModel(keywordAndFacetEntries, currentView, query, drillDowns, Option.<Integer>none()));
        return facetsResponseModel(facetsModels, currentView, query, drillDowns).merge(drillDownsExceptionModel);
    }

    @GET
    @Path("facet")
    public Model facet(@QueryParam("current") @DefaultValue("") String currentView, @QueryParam("query") @DefaultValue("") String query, @QueryParam("facetName") String facetName, @QueryParam("entryCount") Option<Integer> entryCount, @QueryParam("drills") Either<String, DrillDowns> drillDowns) throws IOException {
        final Model drillDownsExceptionModel = drillDowns.map(returns1(drillDownExceptionModel(INVALID_DRILLS_MESSAGE)), returns1(model()));

        final Option<Model> viewOption = find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return model().merge(drillDownsExceptionModel);
        }

        final Option<Keyword<?>> viewHeader = headers(viewOption.get()).find(where(name(), is(facetName)));
        final Keyword<?> facetKeyword = viewHeader.getOrThrow(new RuntimeException("No facet found with name " + facetName));
        final Either<String, Predicate<Record>> queryPredicate = predicateBuilder.build(query, Sequences.<Keyword<?>>one(facetKeyword));
        final int viewDefinitionConfiguredSize = facetKeyword.metadata().get(FACET_ENTRIES).intValue();

        final Sequence<FacetDrillDown> facetDrillDowns = calculateDrillDowns(drillDowns.right(), headers(viewOption.get()));

        final Model facetModel = records.facetResults(queryPredicate.right(), one(facetRequest(facetKeyword)), facetDrillDowns).
                map(toSortedEntriesModels(facetDrillDowns)).
                map(toFacetModel(Collections.<Keyword<?>, Integer>singletonMap(facetKeyword, viewDefinitionConfiguredSize), currentView, query, drillDowns, entryCount)).
                first();

        return model().
                add("facet", facetModel).
                add("drills", drillDowns.map(asString(), asString())).
                merge(drillDownsExceptionModel);
    }

    private Mapper<Facet<FacetEntry>, Pair<Keyword<?>, Sequence<Model>>> toSortedEntriesModels(final Sequence<FacetDrillDown> facetDrillDowns) {
        return new Mapper<Facet<FacetEntry>, Pair<Keyword<?>, Sequence<Model>>>() {
            @Override
            public Pair<Keyword<?>, Sequence<Model>> call(Facet<FacetEntry> facet) throws Exception {
                final Keyword<?> facetKeyword = facet.key();
                final Pair<Sequence<Model>, Sequence<Model>> facetEntries = facet.map(asFacetEntryModel(facetKeyword, facetDrillDowns)).partition(isEntryDrilledDown());
                final Option<FacetDrillDown> drillDownForCurrentFacet = facetDrillDowns.find(drillDownKeyIs(facetKeyword));
                final Sequence<Model> drilledDownEntries = drillDownForCurrentFacet.isDefined() ? facetEntries.first().sortBy(positionIn(drillDownForCurrentFacet.get())) : Sequences.<Model>empty();
                final Sequence<Model> otherEntries = facetEntries.second();
                return Pair.<Keyword<?>, Sequence<Model>>pair(facetKeyword, drilledDownEntries.join(otherEntries));
            }
        };
    }

    private LogicalPredicate<Model> isEntryDrilledDown() {
        return where(value("drilledDown", Boolean.class), is(true));
    }

    private Mapper<Pair<Keyword<?>, Sequence<Model>>, Model> toFacetModel(final Map<Keyword<?>, Integer> keywordAndFacetEntries, final String currentView, final String query, final Either<String, DrillDowns> drillDowns, final Option<Integer> requestedEntryCount) {
        return new Mapper<Pair<Keyword<?>, Sequence<Model>>, Model>() {
            @Override
            public Model call(Pair<Keyword<?>, Sequence<Model>> entriesModels) throws Exception {
                final Keyword<?> facetKeyword = entriesModels.first();
                final Sequence<Model> entries = entriesModels.second();
                final int drilledDownCount = entries.filter(isEntryDrilledDown()).size();
                final int entriesToDisplay = Math.max(requestedEntryCount.getOrElse(keywordAndFacetEntries.get(facetKeyword)), drilledDownCount);
                Model base = model().
                        add("name", facetKeyword.name()).
                        add("class-name", classNameOf(facetKeyword.name())).
                        add("entries", entries.take(entriesToDisplay));
                if (entries.size() > entriesToDisplay) {
                    base = base.add("more", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facetKeyword.name(), Option.some(Integer.MAX_VALUE), drillDowns))).toString());
                }
                if (entries.filter(not(isEntryDrilledDown())).size() > 0 && entries.size() > keywordAndFacetEntries.get(facetKeyword) && entriesToDisplay > entries.size()) {
                    base = base.add("fewer", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facetKeyword.name(), Option.none(Integer.class), drillDowns))).toString());
                }
                return base;
            }
        };
    }

    private Model facetsResponseModel(Sequence<Model> facets, String currentView, String query, Either<String, DrillDowns> drillDowns) {
        return model()
                .add("facets", facets)
                .add("query", query)
                .add("view", currentView)
                .add("drills", drillDowns.map(asString(), asString()));
    }

    private Model drillDownExceptionModel(String message) {
        return model().add("drillDownsException", message);
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
                return facetDrillDown(facetKeyword, facetDrillDown.second());
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

    private Predicate<FacetDrillDown> drillDownKeyIs(final Keyword<?> facetKeyword) {
        return new Predicate<FacetDrillDown>() {
            @Override
            public boolean matches(FacetDrillDown facetDrillDown) {
                return facetDrillDown.key().equals(facetKeyword);
            }
        };
    }

    private Callable1<Model, Integer> positionIn(final FacetDrillDown facetDrillDown) {
        return new Callable1<Model, Integer>() {
            @Override
            public Integer call(Model entryModel) throws Exception {
                return facetDrillDown.indexOf(entryModel.get("displayValue", String.class));
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
                return facet.key().equals(facetKeyword) && facet.contains(stringMappings.toString(facetKeyword.forClass(), facetEntry.first()));
            }
        };
    }

}
