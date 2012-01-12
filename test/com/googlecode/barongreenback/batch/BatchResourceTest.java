package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.ImportCrawlerPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BatchResourceTest extends ApplicationTests {

    @Test
    public void canDeleteTheIndex() throws Exception {
        indexHasCrawlerDefinition();

        deleteTheIndex();

        noCrawlersExist();
    }

    private void indexHasCrawlerDefinition() throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        importPage.model().value(contentOf("crawler.json"));
        importPage.importModel();
    }

    private void deleteTheIndex() throws Exception {
        BatchOperationsPage batchOperationsPage = new BatchOperationsPage(browser);
        batchOperationsPage.delete();
    }

    private void noCrawlersExist() throws Exception {
        CrawlerListPage crawlerListPage = new CrawlerListPage(browser);
        assertThat(crawlerListPage.numberOfCrawlers(), is(0));
    }
}
