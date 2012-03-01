package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.crawler.CrawlerTest;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.AnnotatedBindings;
import com.googlecode.waitrest.Waitrest;
import com.googlecode.yadic.Container;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keywords;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchResourceTest extends ApplicationTests {
    @Test
    public void handlesInvalidQueriesInANiceWay() throws Exception {
        SearchPage searchPage = new SearchPage(browser, "users", "^&%$^%");
        assertThat(searchPage.queryMessage(), Matchers.is("Invalid Query"));
    }

    @Test
    public void supportsDelete() throws Exception {
        SearchPage searchPage = new SearchPage(browser, "users", "", true);
        assertThat(searchPage.numberOfResults(), is(2));
        searchPage = searchPage.delete();
        assertThat(searchPage.numberOfResults(), is(0));
    }

    @Test
    public void supportsQueryAll() throws Exception {
        SearchPage searchPage = new SearchPage(browser, "users", "");
        assertThat(searchPage.numberOfResults(), is(2));
    }

    @Test
    public void supportsQueryForAParticularEntry() throws Exception {
        SearchPage searchPage = new SearchPage(browser, "users", "id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\"");
        assertThat(searchPage.numberOfResults(), is(1));
    }

    @Test
    public void whenAnUnknownViewIsSpecifiedThenNoResultsShouldBeShown() throws Exception {
        SearchPage searchPage = new SearchPage(browser, "UNKNOWN", "");
        assertThat(searchPage.numberOfResults(), is(0));
    }

    @Test
    public void supportsShortcutToUniquePage() throws Exception {
        RequestBuilder requestBuilder = get("/" + AnnotatedBindings.relativeUriOf(method(on(SearchResource.class).shortcut("users", "id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\""))));
        Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(response.header("Location"), Matchers.is("/users/search/unique?query=id%3A%22urn%3Auuid%3Ac356d2c5-f975-4c4d-8e2a-a698158c6ef1%22"));
    }

    @Test
    public void supportsShortcutToListPage() throws Exception {
        RequestBuilder requestBuilder = get("/" + AnnotatedBindings.relativeUriOf(method(on(SearchResource.class).shortcut("users", ""))));
        Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(response.header("Location"), Matchers.is("/users/search/list?query="));
    }

    @Test
    public void shortCut() throws Exception {
        RequestBuilder requestBuilder = get("/" + AnnotatedBindings.relativeUriOf(method(on(SearchResource.class).shortcut("users", "BAD_QUERY"))));
        Response response = application.handle(requestBuilder.build());
        assertThat(response.status(), Matchers.is(Status.SEE_OTHER));
        assertThat(response.header("Location"), Matchers.is("/users/search/list?query=BAD_QUERY"));
    }

    @Before
    public void addSomeData() throws Exception {
        Waitrest waitrest = CrawlerTests.setupServerWithDataFeed();

        final Sequence<Record> recordSequence = CrawlerTest.crawlOnePageOnly().realise();

        application.usingRequestScope(new Callable1<Container, Void>() {
            public Void call(Container container) throws Exception {
                Records records = container.get(BaronGreenbackRecords.class).value();
                Definition users = Definition.constructors.definition("users", keywords(recordSequence));
                records.add(users, recordSequence);
                ModelRepository views = container.get(ModelRepository.class);
                views.set(UUID.randomUUID(), Views.convertToViewModel(users));
                return VOID;
            }
        });

        waitrest.close();
    }
}
