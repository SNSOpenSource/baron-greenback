package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.html.RelativeUrlHandler;
import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.barongreenback.search.ViewSearchPage;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.RedirectHttpHandler;
import com.googlecode.utterlyidle.httpserver.RestServer;
import com.googlecode.waitrest.Restaurant;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.TEXT_XML;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerResourceTest {
    @Test
    public void canSaveAndLoadACrawler() throws Exception {
        CrawlerPage newPage = new CrawlerPage(new RedirectHttpHandler(new RelativeUrlHandler(new WebApplication())));
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
        ImportCrawlerPage importPage = new ImportCrawlerPage(new RedirectHttpHandler(new RelativeUrlHandler(new WebApplication())));
        importPage.model().value(fileContent("crawler.json"));
        CrawlerListPage listPage = importPage.importModel();
        assertThat(listPage.contains("news"), is(true));
    }

    @Test
    public void canCrawlFeedsWithPaginationAndCheckpoint() throws Exception {
        setupServerWithDataFeed();

        HttpHandler handler = new RedirectHttpHandler(new RelativeUrlHandler(new WebApplication()));
        CrawlerPage newPage = new CrawlerPage(handler);
        newPage.update().value("news feed");
        newPage.from().value("http://localhost:9001/data");
        newPage.more().value("//link[@rel='prev-archive']/@href");
        newPage.checkpoint().value("2011-07-19T12:43:20Z");
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
        CrawlerListPage list = newPage.save();
        list.crawl("news feed");

        Thread.sleep(100);

        ViewSearchPage viewSearchPage = new ViewSearchPage(handler, "news feed", "");

        assertThat(viewSearchPage.containsCell("Added user", "title"), is(true));
        assertThat(viewSearchPage.containsCell("Deleted user", "title"), is(true));
        assertThat(viewSearchPage.containsCell("Updated user", "title"), is(true));
        assertThat(viewSearchPage.containsCell("Created user", "title"), is(false));
    }

    private void setupServerWithDataFeed() throws Exception {
        RestServer dataSourceServer = new RestServer(new Restaurant(), defaultConfiguration().port(9001));
        ClientHttpHandler restClient = new ClientHttpHandler();
        restClient.handle(put(dataSourceServer.uri() + "data").withHeader(CONTENT_TYPE, TEXT_XML).withInput(fileContent("atom.xml").getBytes()).build());
        restClient.handle(put(dataSourceServer.uri() + "data/prev").withHeader(CONTENT_TYPE, TEXT_XML).withInput(fileContent("atom-prev.xml").getBytes()).build());
    }

    private String fileContent(String name) {
        return Strings.toString(getClass().getResourceAsStream(name));
    }
}
