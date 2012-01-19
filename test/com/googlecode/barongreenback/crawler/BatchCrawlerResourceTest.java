package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BatchCrawlerResourceTest extends ApplicationTests {
    @Test
    public void canCrawlAll() throws Exception {
        importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));

        JobsListPage jobsListPage = crawlAll(crawlerListPage);

        assertThat(jobsListSize(jobsListPage), is(2));
    }

    @Test
    public void canDeleteAll() throws Exception {
        importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = importCrawlerWithId(UUID.randomUUID(), contentOf("crawler.json"));
        assertThat(crawlerListSize(crawlerListPage), is(2));

        crawlerListPage = deleteAll(crawlerListPage);

        assertThat(crawlerListSize(crawlerListPage), is(0));
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

    private CrawlerListPage deleteAll(CrawlerListPage crawlerListPage) throws Exception {
        return crawlerListPage.deleteAll();
    }

    private int jobsListSize(JobsListPage jobsListPage) {
        return jobsListPage.numberOfJobs();
    }

    private int crawlerListSize(CrawlerListPage crawlerListPage) {
        return crawlerListPage.numberOfCrawlers();
    }

}
