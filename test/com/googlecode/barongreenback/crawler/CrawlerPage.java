package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.html.Html;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CrawlerPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public CrawlerPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Crawler"));
    }

    public CrawlerPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get(urlOf(resource(CrawlerResource.class).get(3))).build()));
    }

    public CrawlerPage update(String value) {
        html.input("//input[@name='update']").value(value);
        return this;
    }

    public CrawlerPage from(String value) {
        html.input("//input[@name='from']").value(value);
        return this;
    }

    public CrawlerListPage save() throws Exception {
        Request request = html.form("//form[contains(@class, 'crawl')]").submit("//input[@type='submit' and @class='save']");
        Response response = httpHandler.handle(request);
        return new CrawlerListPage(httpHandler, response);
    }
}
