package com.googlecode.barongreenback.crawler;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CrawlerImportPage {
    private HttpHandler httpHandler;
    private Html html;

    public CrawlerImportPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(CrawlerResource.class).importForm()))).build()));
    }

    public CrawlerImportPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Import Crawler"));
    }

    public CrawlerListPage importCrawler(String crawlerDefinition) throws Exception {
        html.textarea("//textarea[@name='model']").value(crawlerDefinition);
        return new CrawlerListPage(httpHandler, httpHandler.handle(html.form("//form[contains(@class, 'import')]").submit("//input[@type='submit']")));
    }
}
