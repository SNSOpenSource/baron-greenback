package com.googlecode.barongreenback;

import com.googlecode.barongreenback.crawler.CountDownScheduler;
import com.googlecode.barongreenback.crawler.CrawlerImportPage;
import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.CrawlerPage;
import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.barongreenback.jobs.Scheduler;
import com.googlecode.barongreenback.queues.Completer;
import com.googlecode.barongreenback.queues.CountDownCompleter;
import com.googlecode.barongreenback.search.ViewSearchPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.waitrest.Waitrest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static com.googlecode.barongreenback.crawler.CrawlerTests.serverWithDataFeed;
import static com.googlecode.totallylazy.Closeables.using;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EndToEndTest extends ApplicationTests {
    private Waitrest waitrest;

    @Test
    public void createCrawlerViaUiWithCheckpointOnFirstPage() throws Exception {
        crawlSampleData(createCrawler(Dates.RFC3339().parse("2011-07-19T12:43:25Z")), "newsfeed");
        ViewSearchPage viewSearchPage = view("newsfeed");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(1));

        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(false));
        assertThat(viewSearchPage.containsCell("title", "Updated user"), is(false));
        assertThat(viewSearchPage.containsCell("title", "Created user"), is(false));
    }

    @Test
    public void createCrawlerViaUiWithPaginationAndCheckpointAndCrawlAndThenViewAllRecords() throws Exception {
        crawlSampleData(createCrawler(Dates.RFC3339().parse("2011-07-19T12:43:20Z")), "newsfeed");
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
    public void createCrawlerViaImportWithSubfeedAndThenViewAllRecords() throws Exception {
        crawlSampleData(importCrawler("testCrawler.json"), "test");
        ViewSearchPage viewSearchPage = view("test");

        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(2));

        assertThat(viewSearchPage.containsCell("firstName", 0, "Matt"), is(true));
        assertThat(viewSearchPage.containsCell("firstName", 1, "Dan"), is(true));
        assertThat(viewSearchPage.containsCell("mane", 1, "pink"), is(true));
        assertThat(viewSearchPage.containsCell("shiny", 1, "very"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(true));
    }

    @Before
    public void setupFeed() throws Exception {
        waitrest = serverWithDataFeed();
    }

    @After
    public void shutDownFeed() throws Exception {
        waitrest.close();
    }

    private ViewSearchPage view(String name) throws Exception {
        return new ViewSearchPage(browser, name, "");
    }

    private CrawlerListPage importCrawler(String filename) throws Exception {
        CrawlerImportPage crawlerImportPage = new CrawlerImportPage(browser);
        return crawlerImportPage.importCrawler(Strings.toString(EndToEndTest.class.getResourceAsStream(filename)));
    }

    private JobsListPage crawlSampleData(CrawlerListPage listPage, String name) throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        application.applicationScope().addInstance(CountDownLatch.class, latch).
                decorate(Scheduler.class, CountDownScheduler.class).
                decorate(Completer.class, CountDownCompleter.class);

        JobsListPage jobs = listPage.crawl(name);
        latch.await();
        Thread.sleep(100);
        return jobs;
    }

    private CrawlerListPage createCrawler(Date checkpointValue) throws Exception {
        CrawlerPage newPage = new CrawlerPage(browser);
        newPage.update().value("newsfeed");
        newPage.from().value("http://localhost:9001/data");
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
