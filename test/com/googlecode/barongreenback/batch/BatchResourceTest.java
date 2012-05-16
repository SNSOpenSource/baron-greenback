package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.crawler.CrawlerListPage;
import com.googlecode.barongreenback.crawler.ImportCrawlerPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
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
        importOneCrawler();
        deleteTheIndex();
        assertThat(numberOfCrawlers(), is(0));
    }

    @Test
    public void canBackupAndRestore() throws Exception {
        File backupLocation = newBackupLocation();
        importOneCrawler();

        backupDataTo(backupLocation);
        deleteTheIndex();
        assertThat(numberOfCrawlers(), is(0));

        restoreFrom(backupLocation);
        assertThat(numberOfCrawlers(), is(1));

        delete(backupLocation);
    }

    private void restoreFrom(File backupLocation) throws Exception {
        new BatchOperationsPage(browser).restore(backupLocation.getAbsolutePath());
    }

    private File newBackupLocation() {
        return new File(temporaryDirectory(), randomFilename());
    }

    private BatchOperationsPage backupDataTo(File backupLocation) throws Exception {
        assertThat(backupLocation.exists(), Matchers.is(false));
        BatchOperationsPage page = new BatchOperationsPage(browser).backup(backupLocation.getAbsolutePath());
        assertThat(backupLocation.exists(), Matchers.is(true));
        return page;
    }

    private CrawlerListPage importOneCrawler() throws Exception {
        ImportCrawlerPage page = new ImportCrawlerPage(browser);
        page.model().value(contentOf("crawler.json"));
        CrawlerListPage crawlerListPage = page.importModel();
        assertThat(numberOfCrawlers(), is(1));
        return crawlerListPage;
    }

    private void deleteTheIndex() throws Exception {
        BatchOperationsPage batchOperationsPage = new BatchOperationsPage(browser);
        batchOperationsPage.delete();
    }

    private int numberOfCrawlers() throws Exception {
        return new CrawlerListPage(browser).numberOfCrawlers();
    }
}
