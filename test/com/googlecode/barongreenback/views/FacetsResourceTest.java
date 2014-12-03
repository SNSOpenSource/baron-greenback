package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.lucene.NameBasedIndexFacetingPolicy;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.yadic.Container;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CompositeCrawlerTest.crawlOnePageOnly;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.FIRST;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.STATUS;
import static com.googlecode.barongreenback.views.FacetSection.SHOW_FEWER;
import static com.googlecode.barongreenback.views.FacetSection.SHOW_MORE;
import static com.googlecode.barongreenback.views.FacetSection.facet;
import static com.googlecode.barongreenback.views.FacetSection.facetEntry;
import static com.googlecode.barongreenback.views.FacetSection.facets;
import static com.googlecode.barongreenback.views.FacetSection.singleFacet;
import static com.googlecode.barongreenback.views.ViewsRepository.FACET_ENTRIES;
import static com.googlecode.barongreenback.views.ViewsRepository.SHOW_FACET;
import static com.googlecode.barongreenback.views.ViewsRepository.convertToViewModel;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Keyword.methods.keywords;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.Response.functions.status;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FacetsResourceTest extends ApplicationTests {

    private static final UUID FACETS_VIEW_ID = UUID.randomUUID();
    private Definition usersView;
    private Definition viewWithFacets = facetedViewWhereNumberOfFacetsEntriesIs(3);

    private Definition facetedViewWhereNumberOfFacetsEntriesIs(int value) {
        return definition("facetedView", FIRST.metadata(SHOW_FACET, true).metadata(FACET_ENTRIES, Integer.toString(value)), STATUS.metadata(SHOW_FACET, true).metadata(FACET_ENTRIES, Integer.toString(value)));
    }

    @After
    public void closeApplication() throws IOException {
        application.close();
        waitrest.close();
    }

    @Before
    public void addSomeData() throws Exception {
        final Sequence<Record> recordSequence = crawlOnePageOnly(feed(), feedClient()).realise();

        application.usingRequestScope(new Block<Container>() {
            public void execute(Container container) throws Exception {
                final Records records = container.get(BaronGreenbackRecords.class).value();
                usersView = definition("users", keywords(recordSequence).append(keyword("updated", Date.class)));

                records.add(usersView, recordSequence);
                records.add(viewWithFacets, recordSequence);
                saveView(UUID.randomUUID(), usersView);
                saveView(FACETS_VIEW_ID, viewWithFacets);
            }
        });
    }

    protected Sequence<? extends Module> extraModules() {
        return Sequences.one(new ApplicationScopedModule() {
            @Override
            public Container addPerApplicationObjects(Container container) throws Exception {
                final Container bgbApplicationScope = container.get(BaronGreenbackApplicationScope.class).value();
                bgbApplicationScope.remove(NameBasedIndexFacetingPolicy.class);
                bgbApplicationScope.addInstance(NameBasedIndexFacetingPolicy.class, new NameBasedIndexFacetingPolicy(Predicates.is(viewWithFacets.name())));
                return container;
            }
        });
    }

    private void saveView(final UUID id, final Definition view) {
        application.usingRequestScope(new Block<Container>() {
            public void execute(Container container) throws Exception {
                ModelRepository views = container.get(ModelRepository.class);
                views.set(id, convertToViewModel(view));
            }
        });
    }

    @Test
    public void shouldNotDisplayAnyFacetsIfThereAreNoneConfigured() throws Exception {
        final FacetSection facetSection = facets(browser, usersView.name(), "", "{}");
        assertThat(facetSection.displayedFacets().size(), is(0));
    }

    @Test
    public void shouldDisplayAllFacetsWhenDrillDownsIsEmpty() throws Exception {
        final FacetSection facetSection = facets(browser, viewWithFacets.name(), "", "{}");
        assertThat(facetSection.displayedFacets(), hasItem(facet(FIRST.name()).withEntries("Dan", "Matt", "Olya")));
        assertThat(facetSection.errorMessage(), is(""));
    }

    @Test
    public void shouldDisplayActiveDrillDownsWhenQueryIsEmpty() throws Exception {
        final FacetSection facetSection = facets(browser, viewWithFacets.name(), "", "{\"first\":[\"Olya\", \"Dan\"]}");
        assertThat(facetSection.displayedFacets(), hasItem(facet(FIRST.name()).withEntries("Olya", "Dan", "Matt")));
        assertThat(facetSection.selectedEntries(), contains(facetEntry("Olya"), facetEntry("Dan")));
        assertThat(facetSection.errorMessage(), is(""));
    }

    @Test
    public void shouldDisplayActiveDrillDownsWhenQueryIsPresent() throws Exception {
        final FacetSection facetSection = facets(browser, viewWithFacets.name(), "*a*", "{\"first\":[\"Dan\"]}");
        assertThat(facetSection.displayedFacets(), hasItem(facet(FIRST.name()).withEntries("Dan", "Matt", "Olya")));
        assertThat(facetSection.selectedEntries(), contains(facetEntry("Dan")));
        assertThat(facetSection.errorMessage(), is(""));
    }

    @Test
    public void listShouldDisplayAShowMoreOptionsIfAFacetContainsMoreEntriesThanTheConfiguredOnes() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(1);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = facets(browser, viewWithLimitedEntries.name(), "", "{}");

        assertThat(QueryParameters.parse(facetSection.link(SHOW_MORE)).getValue("entryCount"), is(valueOf(Integer.MAX_VALUE)));
        assertThat(facetSection.clicking(SHOW_MORE), returns(Status.OK));
    }

    @Test
    public void individualFacetShouldDisplayAShowMoreOptionsIfAFacetContainsMoreEntriesThanTheConfiguredOnes() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(1);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = singleFacet(browser, viewWithLimitedEntries.name(), "", "{}");

        assertTrue(facetSection.hasLink(SHOW_MORE));
        assertFalse(facetSection.hasLink(SHOW_FEWER));
        assertThat(QueryParameters.parse(facetSection.link(SHOW_MORE)).getValue("entryCount"), is(valueOf(Integer.MAX_VALUE)));
        assertThat(facetSection.clicking(SHOW_MORE), returns(Status.OK));
    }

    @Test
    public void individualFacetShouldDisplayAShowFewerOptionsIfAFacetIsShowingMoreEntriesThanTheConfiguredOnes() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(1);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = singleFacet(browser, viewWithLimitedEntries.name(), "", Integer.MAX_VALUE, "{}");

        assertFalse(facetSection.hasLink(SHOW_MORE));
        assertTrue(facetSection.hasLink(SHOW_FEWER));
        assertThat(QueryParameters.parse(facetSection.link(SHOW_FEWER)).contains("entryCount"), is(false));
        assertThat(facetSection.clicking(SHOW_FEWER), returns(Status.OK));
    }

    @Test
    public void individualFacetShouldNotDisplayShowMoreOrShowFewerLinksIfTotalNumberOfEntriesIsUnderConfiguredLimit() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(5);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = singleFacet(browser, viewWithLimitedEntries.name(), "", "{}");

        assertFalse(facetSection.hasLink(SHOW_MORE));
        assertFalse(facetSection.hasLink(SHOW_FEWER));
    }

    @Test
    public void shouldReturnTheFacetsWithoutDrillDownsIfTheSpecifiedDrillDownsAreInvalid() throws Exception {
        final FacetSection facetSection = facets(browser, viewWithFacets.name(), "", "invalid drill downs");

        assertThat(facetSection.selectedFacetEntriesCount(), is(0));
        assertThat(facetSection.drillDownsException(), is("Drill downs can't be parsed"));
        assertThat(facetSection.errorMessage(), is("Could not understand your refine-by criteria; showing all results"));
    }

    @Test
    public void facetViewShouldUseViewEntryCountByDefault() throws Exception {
        final FacetSection facetSection = singleFacet(browser, viewWithFacets.name(), "", "{}");

        assertThat(facetSection.facetEntryCount(), is(3));
    }

    @Test
    public void facetViewShouldUseUrlEntryCountWhereSpecified() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(3);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = singleFacet(browser, viewWithFacets.name(), "", 1, "{}");
        assertThat(facetSection.facetEntryCount(), is(1));
    }

    @Test
    public void individualFacetShouldDisplayAllCheckedEntriesInSelectionOrder() throws Exception {
        final Definition viewWithLimitedEntries = facetedViewWhereNumberOfFacetsEntriesIs(1);
        saveView(FACETS_VIEW_ID, viewWithLimitedEntries);

        final FacetSection facetSection = singleFacet(browser, viewWithLimitedEntries.name(), "", "{\"first\":[\"Olya\",\"Dan\"]}");

        assertThat(facetSection.displayedFacets(), contains(facet(FIRST.name()).withEntries("Olya", "Dan")));
        assertThat(facetSection.selectedEntries(), contains(facetEntry("Olya"), facetEntry("Dan")));
    }

    @Test
    public void individualFacetShouldDisplayCheckedEntriesBeforeUncheckedEntries() throws Exception {
        final FacetSection facetSection = singleFacet(browser, viewWithFacets.name(), "", "{\"first\":[\"Dan\",\"Olya\"]}");

        assertThat(facetSection.displayedFacets(), contains(facet(FIRST.name()).withEntries("Dan", "Olya", "Matt")));
        assertThat(facetSection.selectedEntries(), contains(facetEntry("Dan"), facetEntry("Olya")));
    }

    @Test
    public void listShouldDisplayCheckedEntriesBeforeUncheckedEntries() throws Exception {
        final FacetSection facetSection = facets(browser, viewWithFacets.name(), "", "{\"first\":[\"Dan\",\"Olya\"]}");

        assertThat(facetSection.displayedFacets(), hasItem(facet(FIRST.name()).withEntries("Dan", "Olya", "Matt")));
        assertThat(facetSection.selectedEntries(), contains(facetEntry("Dan"), facetEntry("Olya")));
    }

    @Test
    public void individualFacetShouldWorkWhenFacetingAnotherField() throws Exception {
        final FacetSection facetSection = singleFacet(browser, viewWithFacets.name(), STATUS.name(), "", "{\"first\":[\"Dan\"]}");

        assertThat(facetSection.facetEntryCount(), is(1));
    }

    private Matcher<? super Response> returns(Status status) {
        return Matchers.matcher(where(status(), Predicates.is(status)));
    }

}