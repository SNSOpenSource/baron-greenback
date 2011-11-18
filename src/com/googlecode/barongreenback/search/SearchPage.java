package com.googlecode.barongreenback.search;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import org.apache.lucene.queryParser.ParseException;

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
        this(httpHandler, httpHandler.handle(url(view, query).build()));
    }

    public SearchPage(HttpHandler httpHandler, String view, String query, boolean advanced) throws Exception {
        this(httpHandler, httpHandler.handle(url(view, query).query("advanced", advanced).build()));
    }

    private static RequestBuilder url(String view, String query) throws ParseException {
        return get("/" + relativeUriOf(method(on(SearchResource.class).list(view, query))));
    }

    public Number numberOfResults() {
        return html.count("//table[contains(@class, 'results')]/tbody/tr");
    }

    public SearchPage delete() throws Exception {
        Request request = html.form("//form[contains(@class, 'delete')]").submit("descendant::input[@type='submit' and @class='delete']");
        Response response = httpHandler.handle(request);
        return new SearchPage(httpHandler, response);
    }
}
