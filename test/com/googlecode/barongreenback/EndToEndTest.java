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
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static com.googlecode.barongreenback.crawler.CrawlerTests.serverWithDataFeed;
import static com.googlecode.totallylazy.Closeables.using;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EndToEndTest extends ApplicationTests {
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

//        System.out.println(viewSearchPage);
        assertThat(viewSearchPage.resultsSize(), NumberMatcher.is(2));

        assertThat(viewSearchPage.containsCell("firstName", 0, "Dan"), is(true));
        assertThat(viewSearchPage.containsCell("mane", 0, "pink"), is(true));
        assertThat(viewSearchPage.containsCell("shiny", 0, "very"), is(true));
        assertThat(viewSearchPage.containsCell("firstName", 1, "Matt"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Added user"), is(true));
        assertThat(viewSearchPage.containsCell("title", "Deleted user"), is(true));
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

        return using(serverWithDataFeed(), crawlAndWait(latch, listPage, name));

    }

    private static Callable1<Waitrest, JobsListPage> crawlAndWait(final CountDownLatch latch, final CrawlerListPage listPage, final String name) {
        return new Callable1<Waitrest, JobsListPage>() {
            public JobsListPage call(Waitrest restServer) throws Exception {
                JobsListPage jobs = listPage.crawl(name);
                latch.await();
                return jobs;
            }
        };
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
