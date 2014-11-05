package com.googlecode.barongreenback.search;

import com.googlecode.totallylazy.Either;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;

import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class SearchPage {
    private HttpHandler httpHandler;
    private Html html;

    public SearchPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Search"));
    }

    public SearchPage(HttpHandler httpHandler, String view, String query) throws Exception {
        this(httpHandler, httpHandler.handle(url(view, query, "").build()));
    }

    public SearchPage(HttpHandler httpHandler, String view, String query, boolean advanced) throws Exception {
        this(httpHandler, httpHandler.handle(url(view, query, "").query("advanced", advanced).build()));
    }

    public SearchPage(HttpHandler httpHandler, String view, String query, String drillDown) throws Exception {
        this(httpHandler, httpHandler.handle(url(view, query, drillDown).build()));
    }

    private static RequestBuilder url(String view, String query, String drillDownsDocument) {
        Either<String, DrillDowns> drillDowns = parseDrillDowns(drillDownsDocument);
        return get("/" + relativeUriOf(method(on(SearchResource.class).list(view, query, drillDowns)))).query("decorator", "none");
    }

    public Number numberOfResults() {
        return html.count("//table[contains(@class, 'results')]/tbody/tr");
    }

    public int resultCount() {
        return Integer.parseInt(html.selectContent("//meta[@name='resultCount']/@content"));
    }

    public SearchPage delete() throws Exception {
        Request request = html.form("//form[contains(@class, 'delete')]").submit("descendant::input[@type='submit' and @class='delete']");
        Response response = httpHandler.handle(request);
        return new SearchPage(httpHandler, response);
    }

    public String queryMessage() {
        return html.selectContent("//div[contains(@class, 'error')]/span[contains(@class, 'message')]");
    }

    public static Response exportToCsv(HttpHandler httpHandler, String view, String query, String drillDownsDocument) throws Exception {
        final Either<String, DrillDowns> drillDowns = parseDrillDowns(drillDownsDocument);
        return httpHandler.handle(get("/" + relativeUriOf(method(on(SearchResource.class).exportCsv(view, query, drillDowns)))).build());
    }

    public static Either<String, DrillDowns> parseDrillDowns(String drillDownsDocument) {
        Either<String, DrillDowns> drillDowns;
        try {
            drillDowns = right(new DrillDownsActivator(drillDownsDocument).call());
        } catch (Exception e) {
            drillDowns = left(drillDownsDocument);
        }
        return drillDowns;
    }

    public String drillDowns() {
        return html.selectContent("//meta[@name='drills']/@content");
    }
}