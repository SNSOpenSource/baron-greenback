package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.barongreenback.jobs.Scheduler;
import com.googlecode.barongreenback.search.ViewSearchPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.httpserver.RestServer;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static com.googlecode.barongreenback.crawler.CrawlerTests.setupServerWithDataFeed;
import static com.googlecode.totallylazy.Closeables.using;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class BatchCrawlerResourceTest extends ApplicationTests {
    @Test
    public void canCrawlAll() throws Exception {
        importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));

        JobsListPage jobsListPage = crawlAll(crawlerListPage);

        assertThat(jobsListSize(jobsListPage), is(2));
    }

    private CrawlerListPage importCrawlerWithId(UUID uuid, String crawler) throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        importPage.id().value(uuid.toString());
        importPage.model().value(crawler);
        return importPage.importModel();
    }

    private JobsListPage crawlAll(CrawlerListPage crawlerListPage) throws Exception {
        return crawlerListPage.crawlAll();
    }

    private int jobsListSize(JobsListPage jobsListPage) {
        return jobsListPage.numberOfJobs();
    }

}
