package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class BatchCrawlerResourceTest extends ApplicationTests {
    @Test
    public void canCrawlAll() throws Exception {
        JobsListPage jobsListPage = new JobsListPage(browser);
        assertThat(jobsListPage.numberOfJobs(), is(0));

        importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));

        crawlerListPage.crawlAll();

        jobsListPage = new JobsListPage(browser);
        assertThat(jobsListPage.numberOfJobs(), is(greaterThan(0)));
    }

    @Test
    public void canDeleteAll() throws Exception {
        importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        assertThat(crawlerListPage.numberOfCrawlers(), is(2));

        crawlerListPage = crawlerListPage.deleteAll();

        assertThat(crawlerListPage.numberOfCrawlers(), is(0));
    }

    private CrawlerListPage importCrawlerWithId(UUID uuid, String crawler) throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        importPage.id().value(uuid.toString());
        importPage.model().value(crawler);
        return importPage.importModel();
    }

}
