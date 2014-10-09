package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.crawler.CompositeCrawlerTest;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.barongreenback.search.DrillDowns.drillDowns;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Keyword.methods.keywords;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.HttpHeaders.LOCATION;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.Response.methods.header;
import static com.googlecode.utterlyidle.Status.BAD_REQUEST;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class FacetedSearchResourceTest extends ApplicationTests {

    private Definition usersView;

    @Before
    public void addSomeData() throws Exception {
        final Waitrest waitrest = CrawlerTests.serverWithDataFeed();
        final Sequence<Record> recordSequence = CompositeCrawlerTest.crawlOnePageOnly(feed(), feedClient()).realise();

        application.usingRequestScope(new Block<Container>() {
            public void execute(Container container) throws Exception {
                Records records = container.get(BaronGreenbackRecords.class).value();
                usersView = definition("users", keywords(recordSequence).append(keyword("updated", Date.class)));
                records.add(usersView, recordSequence);
                ModelRepository views = container.get(ModelRepository.class);
                views.set(UUID.randomUUID(), ViewsRepository.convertToViewModel(usersView));
            }
        });
        waitrest.close();
    }

    @Test
    public void listShouldReturnAnErrorIfDrillDownIsInvalid() throws Exception {
        final SearchPage searchPage = new SearchPage(browser, "users", "", "invalid drill down");
        assertThat(searchPage.numberOfResults().intValue(), is(3));
        assertThat(searchPage.queryMessage(), is("Could not understand your refine-by criteria; showing results only from your query"));
        assertThat(searchPage.drillDowns(), is("invalid drill down"));
    }

    @Test
    public void listShouldReturnResultsAccordingToDrillDowns() throws Exception {
        final SearchPage searchPage = new SearchPage(browser, "users", "", "{ \"first\": [ \"Dan\" ]}");
        assertThat(searchPage.numberOfResults().intValue(), is(1));
    }

    @Test
    public void listShouldUseBothQueryAndDrillDownsToComputeResults() throws Exception {
        final SearchPage searchPage = new SearchPage(browser, "users", "first:Matt OR first:Dan", "{ \"first\": [ \"Dan\" ]}");
        assertThat(searchPage.numberOfResults().intValue(), is(1));
    }

    @Test
    public void shouldNotShortcutIfDrillDownIsInvalid() throws Exception {
        final RequestBuilder requestBuilder = get("/" + relativeUriOf(method(on(SearchResource.class).
                shortcut("users", "Dan", Either.<String, DrillDowns>left("invalid drill down")))));
        final Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(header(response, LOCATION), startsWith("/users/search/list"));
    }

    @Test
    public void shouldNotShortcutIfDrillDownIsValid() throws Exception {
        final Map<String, List<String>> drillDownMap = singletonMap("first", asList("Dan"));
        final RequestBuilder requestBuilder = get("/" + relativeUriOf(method(on(SearchResource.class).
                shortcut("users", "Dan", Either.<String, DrillDowns>right(drillDowns(drillDownMap))))));
        final Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(header(response, LOCATION), startsWith("/users/search/list"));
    }

    @Test
    public void shouldShortcutIfDrillDownIsValidButNoDrillDownHasBeenSpecified() throws Exception {
        final DrillDowns emptyDrillDowns = drillDowns(Collections.<String, List<String>>emptyMap());
        final RequestBuilder requestBuilder = get("/" + relativeUriOf(method(on(SearchResource.class).
                shortcut("users", "id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\"", Either.<String, DrillDowns>right(emptyDrillDowns)))));
        final Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(header(response, LOCATION), startsWith("/users/search/unique"));
    }

    @Test
    public void exportCsvShouldReturnAnErrorWhenRequestingCsvWithAnInvalidDrillDown() throws Exception {
        final Response csvResponse = SearchPage.exportToCsv(browser, "users", "", "invalid drill down");
        assertThat(csvResponse.status(), is(BAD_REQUEST));
    }

    @Test
    public void canExportToCsvAccordingToDrillDowns() throws Exception {
        final Map<String, List<String>> drillDownMap = singletonMap("first", asList("Dan", "Matt"));
        final RequestBuilder requestBuilder = get("/" + relativeUriOf(method(on(SearchResource.class).
                exportCsv("users", "", Either.<String, DrillDowns>right(drillDowns(drillDownMap))))));
        final Response response = application.handle(requestBuilder.build());
        final InputStream resourceAsStream = FacetedSearchResourceTest.class.getResourceAsStream("csvTest.csv");
        final String expected = Strings.toString(resourceAsStream);
        assertThat(response.entity().toString(), Matchers.is(expected));
    }
}
