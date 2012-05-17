package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.ImportCrawlerPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.totallylazy.Files;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.randomFilename;
import static com.googlecode.totallylazy.Files.temporaryDirectory;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BatchResourceTest extends ApplicationTests {
    @Test
    public void canDeleteTheIndex() throws Exception {
        indexHasCrawlerDefinition();
        deleteTheIndex();
        noCrawlersExist();
    }

    @Test
    public void canBackupStore() throws Exception {
        BatchOperationsPage batchOperationsPage = new BatchOperationsPage(browser);
        File backupLocation = new File(temporaryDirectory(), randomFilename());

        assertThat(backupLocation.exists(), Matchers.is(false));
        batchOperationsPage.backup(backupLocation.getAbsolutePath());
        assertThat(backupLocation.exists(), Matchers.is(true));

        delete(backupLocation);
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
