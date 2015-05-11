package com.sky.sns.barongreenback.crawler;

import com.sky.sns.barongreenback.schedules.ScheduleListPage;
import com.sky.sns.barongreenback.shared.ApplicationTests;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.junit.Test;

import java.util.UUID;

import static com.sky.sns.barongreenback.crawler.CrawlerTests.contentOf;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class BatchCrawlerResourceTest extends ApplicationTests {
    @Test
    public void canCrawlAll() throws Exception {
        ScheduleListPage scheduleListPage = new ScheduleListPage(browser);
        final int existingJobs = scheduleListPage.numberOfJobs();

        importCrawlerWithId(randomUUID(), contentOf("crawler.json"), browser);
        CrawlerListPage crawlerListPage = importCrawlerWithId(randomUUID(), contentOf("crawler.json"), browser);

        crawlerListPage.crawlAll();

        scheduleListPage = new ScheduleListPage(browser);
        assertThat(scheduleListPage.numberOfJobs(), is(greaterThan(existingJobs)));
    }

    @Test
    public void ignoresDisabledCrawlersWhenCrawlingAll() throws Exception {
        ScheduleListPage scheduleListPage = new ScheduleListPage(browser);
        final int existingJobs = scheduleListPage.numberOfJobs();

        final UUID crawlerId = randomUUID();
        CrawlerListPage crawlerListPage = importCrawlerWithId(crawlerId, contentOf("crawler.json"), browser);
        crawlerListPage.disable("news");

        crawlerListPage.crawlAll();

        scheduleListPage = new ScheduleListPage(browser);
        assertThat(scheduleListPage.numberOfJobs(), is(existingJobs));
    }

    @Test
    public void canDeleteAll() throws Exception {
        importCrawlerWithId(randomUUID(), contentOf("crawler.json"), browser);
        CrawlerListPage crawlerListPage = importCrawlerWithId(randomUUID(), contentOf("crawler.json"), browser);
        assertThat(crawlerListPage.numberOfCrawlers(), is(2));

        crawlerListPage = crawlerListPage.deleteAll();

        assertThat(crawlerListPage.numberOfCrawlers(), is(0));
    }

    public static CrawlerListPage importCrawlerWithId(UUID uuid, String crawler, HttpClient browser) throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        importPage.id().value(uuid.toString());
        importPage.model().value(crawler);
        return importPage.importModel();
    }

}
