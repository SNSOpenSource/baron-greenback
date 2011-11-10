package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.jobs.JobsListPage;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Link;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CrawlerListPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public CrawlerListPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(CrawlerResource.class).list()))).build()));
    }

    public CrawlerListPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Crawlers"));
    }

    public boolean contains(String name) {
        return html.selectContent("//a[contains(@class, 'update')]/text()").equals(name);
    }

    public CrawlerPage edit(String name) throws Exception {
        Request request = linkFor(name).click();
        return new CrawlerPage(httpHandler, httpHandler.handle(request));
    }

    public Link linkFor(String crawlerName) {
        return html.link(linkTo(crawlerName));
    }

    private String linkTo(String crawlerName) {
        return "//a[contains(@class, 'update') and text() = '" + crawlerName + "']";
    }

    public JobsListPage crawl(String name) throws Exception {
        Request request = html.form(formFor(name, "crawl")).submit(button("crawl"));
        Response response = httpHandler.handle(request);
        return new JobsListPage(httpHandler, response);
    }

    public CrawlerListPage reset(String name) throws Exception {
        Request request = html.form(formFor(name, "reset")).submit(button("reset"));
        Response response = httpHandler.handle(request);
        return new CrawlerListPage(httpHandler, response);
    }

    private String button(String name) {
        return format("descendant::input[@type='submit' and @class='%s']", name);
    }

    private String formFor(String crawlerName, String formName) {
        return format("//tr[%s]/descendant::form[contains(@class, '%s')]", linkTo(crawlerName), formName);
    }

    public boolean isResettable(String name) {
        return html.contains(formFor(name, "reset"));
    }
}