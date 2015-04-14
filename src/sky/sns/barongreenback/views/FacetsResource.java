package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.search.DrillDowns;
import com.googlecode.barongreenback.search.PredicateBuilder;
import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetDrillDown;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
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
import static com.googlecode.barongreenback.search.RecordsService.unalias;
import static com.googlecode.barongreenback.views.ViewsRepository.SHOW_FACET;
import static com.googlecode.barongreenback.views.ViewsRepository.find;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.lazyrecords.FacetDrillDown.facetDrillDown;
import static com.googlecode.lazyrecords.FacetRequest.constructors.facetRequest;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Callables.returns1;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.contains;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Produces({MediaType.TEXT_HTML})
@Path("facets")
public class FacetsResource {
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
    public Model list(@QueryParam("current") @DefaultValue("") String currentView, @QueryParam("query") @DefaultValue("") String query, @QueryParam("drills") @DefaultValue("") Either<String, DrillDowns> drillDowns) throws IOException {
        final Option<Model> viewOption = find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return facetsResponseModel(Sequences.<Model>empty(), currentView, query, drillDowns).merge(drillDownsExceptionModel(drillDowns));
        }

        final Sequence<Model> facetsModels = facetResults(currentView, query, drillDowns, viewOption.get(), Option.<Integer>none());
        return facetsResponseModel(facetsModels, currentView, query, drillDowns).merge(drillDownsExceptionModel(drillDowns));
    }

    @GET
    @Path("facet")
    public Model facet(@QueryParam("current") @DefaultValue("") String currentView, @QueryParam("query") @DefaultValue("") String query, @QueryParam("facetName") String facetName, @QueryParam("entryCount") Option<Integer> entryCount, @QueryParam("drills") @DefaultValue("") Either<String, DrillDowns> drillDowns) throws IOException {
        final Option<Model> viewOption = find(modelRepository, currentView);
        if (viewOption.isEmpty()) {
            return model().merge(drillDownsExceptionModel(drillDowns));
        }

        final Sequence<Model> results = facetResults(currentView, query, drillDowns, viewOption.get(), entryCount);
        final Model facetModel = results.filter(where(value("name", String.class), is(facetName))).headOption().getOrThrow(new RuntimeException("No facet found with name " + facetName));

        return model().
                add("facet", facetModel).
                add("drills", drillDowns.map(asString(), asString())).
                merge(drillDownsExceptionModel(drillDowns));
    }

    private Sequence<Model> facetResults(String currentView, String query, Either<String, DrillDowns> drillDowns, Model view, Option<Integer> requestedEntryCount) throws IOException {
        final Sequence<Keyword<?>> viewHeaders = headers(view).map(unalias());
        final Either<String, Predicate<Record>> queryPredicate = predicateBuilder.build(RecordsService.prefixQueryWithImplicitViewQuery(view, query), viewHeaders);

        final Map<Keyword<?>, Integer> keywordAndConfiguredCounts = Maps.map(viewHeaders.filter(where(metadata(SHOW_FACET), is(notNullValue(Boolean.class).and(is(true)))))
                .map(ViewsRepository.toKeywordAndFacetEntries()).map(Callables.<Keyword<?>, String, Integer>second(Callables.<String, Number, Integer>compose(Numbers.valueOf, Numbers.intValue))));
        final Sequence<FacetDrillDown> facetDrillDowns = calculateDrillDowns(drillDowns.rightOption().getOrElse(DrillDowns.empty()), viewHeaders);
        final Sequence<Facet<FacetEntry>> facets = records.facetResults(queryPredicate.right(), sequence(keywordAndConfiguredCounts.keySet()).map(facetRequest()), facetDrillDowns);
        return facets.map(toSortedEntriesModels(facetDrillDowns)).map(toFacetModel(keywordAndConfiguredCounts, currentView, query, drillDowns, requestedEntryCount));
    }

    private Model drillDownsExceptionModel(Either<String, DrillDowns> drillDowns) {
        return drillDowns.map(returns1(model().add("drillDownsException", "Drill downs can't be parsed")), returns1(model()));
    }

    private Mapper<Facet<FacetEntry>, Pair<Keyword<?>, DisplayedFacetEntries>> toSortedEntriesModels(final Sequence<FacetDrillDown> facetDrillDowns) {
        return new Mapper<Facet<FacetEntry>, Pair<Keyword<?>, DisplayedFacetEntries>>() {
            @Override
            public Pair<Keyword<?>, DisplayedFacetEntries> call(Facet<FacetEntry> facet) throws Exception {
                final Keyword<?> facetKeyword = facet.key();
                final Pair<Sequence<Model>, Sequence<Model>> facetEntries = facet.map(asFacetEntryModel(facetKeyword, facetDrillDowns)).partition(isEntryDrilledDown());
                final Option<FacetDrillDown> drillDownForCurrentFacet = facetDrillDowns.find(where(Group.functions.<Keyword<?>, String>key(), Predicates.<Keyword<?>>is(facetKeyword)));
                final Sequence<Model> drilledDownEntries = drillDownForCurrentFacet.isDefined() ? facetEntries.first().sortBy(positionIn(drillDownForCurrentFacet.get())) : Sequences.<Model>empty();
                final Sequence<Model> otherEntries = facetEntries.second();
                return Pair.<Keyword<?>, DisplayedFacetEntries>pair(facetKeyword, new DisplayedFacetEntries(drilledDownEntries, otherEntries));
            }
        };
    }

    private LogicalPredicate<Model> isEntryDrilledDown() {
        return where(value("drilledDown", Boolean.class), is(true));
    }

    private Mapper<Pair<Keyword<?>, DisplayedFacetEntries>, Model> toFacetModel(final Map<Keyword<?>, Integer> keywordAndFacetEntries, final String currentView, final String query, final Either<String, DrillDowns> drillDowns, final Option<Integer> requestedEntryCount) {
        return new Mapper<Pair<Keyword<?>, DisplayedFacetEntries>, Model>() {
            @Override
            public Model call(Pair<Keyword<?>, DisplayedFacetEntries> entriesModels) throws Exception {
                final Keyword<?> facetKeyword = entriesModels.first();
                final DisplayedFacetEntries displayedFacetEntries = entriesModels.second();
                int drilledDownCount = displayedFacetEntries.drillDownCount();
                final int entriesCount = displayedFacetEntries.allEntries().size();
                final int entriesToDisplay = Math.max(requestedEntryCount.getOrElse(keywordAndFacetEntries.get(facetKeyword)), drilledDownCount);
                Model base = model().
                        add("name", facetKeyword.name()).
                        add("class-name", classNameOf(facetKeyword.name())).
                        add("entries", displayedFacetEntries.allEntries().take(entriesToDisplay));
                if (entriesCount > entriesToDisplay) {
                    base = base.add("more", redirector.absoluteUriOf(method(on(FacetsResource.class).facet(currentView, query, facetKeyword.name(), Option.some(Integer.MAX_VALUE), drillDowns))).toString());
                }
                if ((entriesCount - drilledDownCount) > 0 && entriesCount > keywordAndFacetEntries.get(facetKeyword) && entriesToDisplay > entriesCount) {
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
                .add("drills", drillDowns.map(asString(), asString()))
                .add("hasFilters", drillDowns.isRight() && !drillDowns.right().equals(DrillDowns.empty()));
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
                final String value = stringMappings.toString(facetKeyword.forClass(), facetEntry.first());
                final boolean drilledDown = facetDrillDowns.exists(where(Group.functions.<Keyword<?>, String>key(), Predicates.<Keyword<?>>is(facetKeyword)).and(contains(value)));
                return model()
                        .add("value", value)
                        .add("displayValue", facetEntry.first())
                        .add("itemsTotal", facetEntry.second())
                        .add("drilledDown", drilledDown);
            }
        };
    }

    private class DisplayedFacetEntries {
        private final Sequence<Model> allEntries;
        private final int drillDownCount;

        protected DisplayedFacetEntries(Sequence<Model> drilledDownEntries, Sequence<Model> otherEntries) {
            this.allEntries = drilledDownEntries.join(otherEntries);
            drillDownCount = drilledDownEntries.size();
        }

        public Sequence<Model> allEntries() {
            return allEntries;
        }

        public int drillDownCount() {
            return drillDownCount;
        }
    }
}
