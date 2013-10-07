package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.schedules.ScheduleListPage;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Link;

import java.util.UUID;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CrawlerListPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public CrawlerListPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(CrawlerDefinitionResource.class).list(Option.<String>none())))).build()));
    }

    public CrawlerListPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Crawlers"));
    }

    public boolean contains(String name) {
        return html.contains(crawlerRowFor(name));
    }

    public CrawlerPage edit(String name) throws Exception {
        Request request = editButtonFor(name).click();
        return new CrawlerPage(httpHandler, httpHandler.handle(request));
    }

    public Link editButtonFor(String crawlerName) {
        return html.link(linkTo(crawlerName));
    }

    private String linkTo(String crawlerName) {
        return format("%s/descendant::a[contains(@class, 'edit')]", crawlerRowFor(crawlerName));
    }

    private String crawlerRowFor(String crawlerName) {
        return format("descendant::tr[@class='crawler' and td[@class='name' and text()='%s']]", crawlerName);
    }


    public String crawlAndWait(UUID id) throws Exception {
        Response response = httpHandler.handle(post("/" + relativeUriOf(method(on(CrawlerDefinitionResource.class).crawl(id)))).form("id", id).build());
        if (response.status().code() >= 400) {
            throw new RuntimeException(String.format("Problem crawling \n%s", response));
        }
        return response.entity().toString();
    }

    public String crawlAndCreateViewAndWait(UUID id) throws Exception {
        Response response = httpHandler.handle(post("/" + relativeUriOf(method(on(CrawlerDefinitionResource.class).crawlAndCreateView(id)))).form("id", id).build());
        if (response.status().code() >= 400) {
            throw new RuntimeException(String.format("Problem crawling \n%s", response));
        }
        return response.entity().toString();
    }

    public ScheduleListPage crawl(String name) throws Exception {
        return goToJobsList(html.form(formFor(name, "crawl")).submit(button("crawl")));
    }

    public ScheduleListPage crawlAndCreateView(String name) throws Exception {
        return goToJobsList(html.form(formFor(name, "crawl-and-create-view")).submit(button("crawl-and-create-view")));
    }

    public ScheduleListPage crawlAll() throws Exception {
        return goToJobsList(html.form(singleForm("crawlAll")).submit(button("crawlAll")));
    }

    private ScheduleListPage goToJobsList(Request request) throws Exception {
        Response response = httpHandler.handle(request);
        return new ScheduleListPage(httpHandler, response);
    }

    public CrawlerListPage deleteAll() throws Exception {
        return goToCrawlerListPage(html.form(singleForm("deleteAll")).submit(button("deleteAll")));
    }

    public CrawlerListPage reset(String name) throws Exception {
        return goToCrawlerListPage(html.form(formFor(name, "reset")).submit(button("reset")));
    }

    private CrawlerListPage goToCrawlerListPage(Request request) throws Exception {
        Response response = httpHandler.handle(request);
        return new CrawlerListPage(httpHandler, response);
    }

    public int numberOfCrawlers() {
        return html.count("//tr[@class='crawler']").intValue();
    }

    private String button(String name) {
        return format("descendant::input[@type='submit' and @class='%s']", name);
    }

    private String formFor(String crawlerName, String formName) {
        return format("%s/descendant::form[contains(@class, '%s')]", crawlerRowFor(crawlerName), formName);
    }

    private String singleForm(String formName) {
        return format("//form[contains(@class, '%s')]", formName);
    }

    public boolean isResettable(String name) {
        return html.input(formFor(name, "reset") + "/" + button("reset")).enabled();
    }

    public boolean isEnabled(String name) {
        return html.contains(formFor(name, "disable"));
    }

    public CrawlerListPage copy(String crawlerName) throws Exception {
        return new CrawlerListPage(httpHandler, httpHandler.handle(html.form(formFor(crawlerName, "copy")).submit(button("copy"))));
    }

    public CrawlerListPage changeCrawler(Class<? extends Crawler> crawlerClass) throws Exception {
        html.select("//select[@class='crawler']").value(crawlerClass.getName());
        return new CrawlerListPage(httpHandler, httpHandler.handle(html.form("//form[contains(@class, 'activeCrawler')]").submit("descendant::input[@value='change']")));
    }
}