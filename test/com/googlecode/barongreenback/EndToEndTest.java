package com.googlecode.barongreenback;

import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.CrawlerPage;
import com.googlecode.barongreenback.search.ViewSearchPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.barongreenback.views.ViewEditPage;
import com.googlecode.barongreenback.views.ViewListPage;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.matchers.Matchers.matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EndToEndTest extends ApplicationTests {
    @Test
    public void paginationWorksCorrectly() throws Exception {
        crawlSampleData(createCrawler(Dates.RFC3339().parse("2011-07-19T12:43:21Z")), "newsfeed");
        ViewSearchPage page1 = viewWithPageSize("newsfeed", 1);
        assertThat(page1.resultsSize(), NumberMatcher.is(1));

        ViewSearchPage page2 = page1.nextPage();
        assertThat(page2.resultsSize(), NumberMatcher.is(1));

        ViewSearchPage page3 = page2.nextPage();
        assertThat(page3.resultsSize(), NumberMatcher.is(1));
        assertThat(page3.hasNextPage(), is(false));
    }

    @Test
    public void createCrawlerViaUiWithCheckpointOnFirstPage() throws Exception {
        final CrawlerListPage crawler = createCrawler(Dates.RFC3339().parse("2011-07-19T12:43:26Z"));
        crawlSampleData(crawler, "newsfeed");
        ViewSearchPage viewSearchPage = view("newsfeed");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(1));

        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(false));
        assertThat(viewSearchPage.containsCell("title", "Updated user"), is(false));
        assertThat(viewSearchPage.containsCell("title", "Created user"), is(false));
    }

    @Test
    public void createCrawlerViaUiWithPaginationAndCheckpointAndCrawlAndThenViewAllRecords() throws Exception {
        crawlSampleData(createCrawler(Dates.RFC3339().parse("2011-07-19T12:43:21Z")), "newsfeed");
        ViewSearchPage viewSearchPage = view("newsfeed");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(3));

        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Updated user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Created user"), is(false));
    }

    @Test
    public void createCrawlerViaUiWithPaginationAndWithoutCheckpointAndCrawlAndThenViewAllRecords() throws Exception {
        crawlSampleData(createCrawler(null), "newsfeed");
        ViewSearchPage viewSearchPage = view("newsfeed");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(4));

        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Updated user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Created user"), is(true));
    }

    @Test
    public void viewAllRecordsWithViewAlias() throws Exception {
        crawlSampleData(createCrawler(null), "newsfeed");
        ViewEditPage page = editView("newsfeed");
        page.fieldAlias(1).value("event title");
        page.save();

        ViewSearchPage viewSearchPage = view("newsfeed");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(4));

        assertThat(viewSearchPage.containsCell("event_title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", "Deleted user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", "Updated user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", "Created user"), is(true));
    }

    @Test
    public void sortShouldWorkForAliasedColumns() throws Exception {
        crawlSampleData(createCrawler(null), "newsfeed");
        ViewEditPage page = editView("newsfeed");
        page.fieldAlias(1).value("event title");
        page.save();

        HttpClient browserThatRequestsSortOrderForEventTitle = new HttpClient() {
            @Override
            public Response handle(Request request) throws Exception {
                return browser.handle(RequestBuilder.modify(request).query(Sorter.SORT_COLUMN_QUERY_PARAM, "event title").query(Sorter.SORT_DIRECTION_QUERY_PARAM, Sorter.ASCENDING_SORT_DIRECTION).build());
            }
        };
        ViewSearchPage viewSearchPage = new ViewSearchPage(browserThatRequestsSortOrderForEventTitle, "newsfeed", 4);

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(4));

        assertThat(viewSearchPage.containsCell("event_title", 0, "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", 1, "Created user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", 2, "Deleted user"), is(true));
        assertThat(viewSearchPage.containsCell("event_title", 3, "Updated user"), is(true));
    }

    @Test
    public void createCrawlerViaImportWithSubfeedAndThenViewAllRecords() throws Exception {
//        crawlSampleData(importCrawler("testCrawler.json"), "test");
        crawlSampleData(importCrawler(EndToEndTest.class.getResourceAsStream("testQueuesCrawler.json")), "test");
        ViewSearchPage viewSearchPage = view("test");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(2));

        assertThat(viewSearchPage.containsCell("firstName", 0, "Matt"), is(true));
        assertThat(viewSearchPage.containsCell("firstName", 1, "Dan"), is(true));
        assertThat(viewSearchPage.containsCell("mane", 1, "pink"), is(true));
        assertThat(viewSearchPage.containsCell("shiny", 1, "very"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(true));
    }

    @Test
    public void createCrawlerViaUiWithoutCheckpointAndCrawlThenViewCheckpoint() throws Exception {
        final CrawlerListPage crawler = createCrawler(null);
        assertThat(checkpointValue(), matcher(empty()));
        crawlSampleData(crawler, "newsfeed");
        assertThat(checkpointValue(), matcher(Predicates.not(empty())));
    }

    private String checkpointValue() throws Exception {
        CrawlerListPage crawlerListPage = new CrawlerListPage(browser);
        final Request request = crawlerListPage.editButtonFor("newsfeed").click();
        final CrawlerPage crawlerPage = new CrawlerPage(browser, browser.handle(request));
        return crawlerPage.checkpoint().value();
    }

    private ViewSearchPage viewWithPageSize(String name, int pageSize) throws Exception {
        return new ViewSearchPage(browser, name, pageSize);
    }

    private ViewSearchPage view(String name) throws Exception {
        return new ViewSearchPage(browser, name, "");
    }

    private ViewEditPage editView(String viewName) throws Exception {
        ViewListPage listPage = new ViewListPage(browser);
        final UUID uuid = listPage.uuidFor(viewName);
        return new ViewEditPage(browser, uuid);
    }

    private CrawlerListPage createCrawler(Date checkpointValue) throws Exception {
        CrawlerPage newPage = new CrawlerPage(browser);
        newPage.update().value("newsfeed");
        newPage.from().value(feed().toString());
        newPage.more().value("//link[@rel='prev-archive']/@href");
        newPage.checkpoint().value(checkpointValue == null ? "" : Dates.RFC3339().format(checkpointValue));
        newPage.checkpointType().value(checkpointValue == null ? String.class.getName() : checkpointValue.getClass().getName());
        newPage.recordName().value("/feed/entry");

        newPage.keyword(1).value("title");
        newPage.alias(1).value("");
        newPage.group(1).value("group1");
        newPage.type(1).value(String.class.getName());
        newPage.unique(1).check();
        newPage.visible(1).check();
        newPage.subfeed(1).uncheck();
        newPage.subfeed(1).uncheck();
        newPage.checkpoint(1).uncheck();

        newPage.keyword(2).value("updated");
        newPage.alias(2).value("");
        newPage.group(2).value("");
        newPage.type(2).value(Date.class.getName());
        newPage.unique(2).uncheck();
        newPage.visible(2).uncheck();
        newPage.subfeed(2).uncheck();
        newPage.checkpoint(2).check();

        return newPage.save();
    }

}