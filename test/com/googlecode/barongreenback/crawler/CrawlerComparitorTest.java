package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.ShowAndTell;
import com.googlecode.barongreenback.batch.BatchOperationsPage;
import com.googlecode.barongreenback.batch.BatchResourceTest;
import com.googlecode.barongreenback.search.SearchPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static com.googlecode.totallylazy.Pair.pair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class CrawlerComparitorTest extends ApplicationTests {
    private static final UUID CRAWLER_ID = UUID.fromString("77916239-0dfe-4217-9e2a-ceaa9e5bed42");

    public Pair<String, String> compare(String sequentialDefinition, String queuesDefinition, String name) throws Exception {
        deleteAll();
        changeCrawlerTo(SequentialCrawler.class);
        importAndCrawl(sequentialDefinition, name);
        String sequentialResult = csvExport(name);

        deleteAll();
        changeCrawlerTo(QueuesCrawler.class);
        importAndCrawl(queuesDefinition, name);
        String queuesResult = csvExport(name);

        return pair(sequentialResult, queuesResult);
    }

    private void deleteAll() throws Exception {
        BatchResourceTest.verifySuccess(new BatchOperationsPage(browser).deleteAll());
    }

    private CrawlerListPage changeCrawlerTo(Class<? extends Crawler> crawlerClass) throws Exception {
        return new CrawlerListPage(browser).changeCrawler(crawlerClass);
    }

    private String csvExport(String name) throws Exception {
        return new SearchPage(browser, name, "").exportToCsv(name, "");
    }

    private String importAndCrawl(String definition, String name) throws Exception {
        CrawlerListPage crawlerListPage = new CrawlerImportPage(browser).importCrawler(definition, Option.some(CRAWLER_ID));
        assertThat(crawlerListPage.contains(name), is(true));
        return crawlerListPage.crawlAndWait(CRAWLER_ID);
    }

    @Test
    @Ignore("manual")
    public void compareCrawlers() throws Exception {
        String bbc = ShowAndTell.bbcDefinition();
        Pair<String, String> results = compare(bbc, bbc, "news");

        Files.write(results.first().getBytes(), new File("/home/dev/Desktop/sequential.csv"));
        Files.write(results.second().getBytes(), new File("/home/dev/Desktop/queues.csv"));

        assertEquals(results.first(), results.second());
    }
}
