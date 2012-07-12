package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.queues.QueuesPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CrawlerTests.contentOf;
import static com.googlecode.totallylazy.Closeables.using;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CrawlerDefinitionResourceTest extends ApplicationTests {
    @Test
    public void canResetACrawler() throws Exception {
        CrawlerPage newPage = new CrawlerPage(browser);
        newPage.update().value("news");
        newPage.checkpoint().value("some value");
        newPage.checkpointType().value(Date.class.getName());
        CrawlerListPage list = newPage.save();
        assertThat(list.isResettable("news"), is(true));
        list = list.reset("news");
        assertThat(list.isResettable("news"), is(false));
        CrawlerPage edit = list.edit("news");
        assertThat(edit.checkpoint().value(), is(""));
        assertThat(edit.checkpointType().value(), is(String.class.getName()));
    }

    @Test
    public void canSaveAndLoadACrawler() throws Exception {
        CrawlerPage newPage = new CrawlerPage(browser);
        newPage.update().value("news");
        newPage.from().value("http://feeds.bbci.co.uk/news/rss.xml");
        newPage.more().value("//link[@rel='prev-archive']/@href");
        newPage.recordName().value("/rss/channel/item");
        newPage.keyword(1).value("title");
        newPage.alias(1).value("foo");
        newPage.group(1).value("foo");
        newPage.type(1).value(String.class.getName());
        newPage.unique(1).check();
        newPage.visible(1).uncheck();
        newPage.subfeed(1).uncheck();
        CrawlerListPage list = newPage.save();

        CrawlerPage edit = list.edit("news");
        assertThat(edit.update().value(), is("news"));
        assertThat(edit.from().value(), is("http://feeds.bbci.co.uk/news/rss.xml"));
        assertThat(edit.more().value(), is("//link[@rel='prev-archive']/@href"));
        assertThat(edit.recordName().value(), is("/rss/channel/item"));
        assertThat(edit.keyword(1).value(), is("title"));
        assertThat(edit.alias(1).value(), is("foo"));
        assertThat(edit.group(1).value(), is("foo"));
        assertThat(edit.type(1).value(), is(String.class.getName()));
        assertThat(edit.unique(1).checked(), is(true));
        assertThat(edit.visible(1).checked(), is(false));
        assertThat(edit.subfeed(1).checked(), is(false));
    }

    @Test
    public void canImportCrawlerInJsonFormat() throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        importPage.model().value(contentOf("crawler.json"));
        CrawlerListPage listPage = importPage.importModel();
        assertThat(listPage.contains("news"), is(true));
    }

    @Test
    public void canImportCrawlerWithId() throws Exception {
        ImportCrawlerPage importPage = new ImportCrawlerPage(browser);
        String id = UUID.randomUUID().toString();
        importPage.id().value(id);
        importPage.model().value(contentOf("crawler.json"));
        CrawlerListPage listPage = importPage.importModel();
        assertThat(listPage.linkFor("news").value(), containsString(id));
    }

    @Test
    public void canEnabledAndDisableCrawler() throws Exception {
        CrawlerPage newPage = new CrawlerPage(browser);
        newPage.update().value("enabled crawler");
        newPage.enabled().check();
        CrawlerListPage list = newPage.save();
        assertThat(list.isEnabled("enabled crawler"), is(true));
        CrawlerPage edit = list.edit("enabled crawler");
        edit.enabled().uncheck();
        assertThat(edit.save().isEnabled("enabled crawler"), is(false));
    }

    @Test
    public void onlyCrawlEnabled() throws Exception {
        UUID disabledCrawlerId = randomUUID();
        String disabled = "disabled";

        CrawlerListPage list = BatchCrawlerResourceTest.importCrawlerWithId(disabledCrawlerId, namedCrawler(disabled, false).toString(), browser);
        list.crawl(disabled);

        QueuesPage queuesPage = new QueuesPage(browser);
        assertThat(queuesPage.responseStatusFor(disabledCrawlerId), is(Status.FORBIDDEN.code()));
    }

    @Test
    public void canCopyACrawlerDefinition() throws Exception {
        CrawlerListPage list = BatchCrawlerResourceTest.importCrawlerWithId(randomUUID(), namedCrawler("crawler", true).toString(), browser);
        list = list.copy("crawler");
        assertThat(list.contains("copy of crawler"), is(true));
        assertThat(list.isEnabled("copy of crawler"), is(false));
    }

    private Model namedCrawler(String name, boolean enabled) {
        String crawler = contentOf("crawler.json");
        Model model = Model.parse(crawler);
        model.get("form", Model.class).set("update", name);
        model.get("form", Model.class).set("enabled", enabled);
        return model;
    }
}
