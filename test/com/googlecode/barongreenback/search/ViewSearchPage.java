package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.html.Html;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ViewSearchPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public ViewSearchPage(HttpHandler httpHandler, String view, String query) throws Exception {
        this(httpHandler, httpHandler.handle(get(urlOf(resource(SearchResource.class).list(view, query))).build()));
    }

    public ViewSearchPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Search"));
    }

    public boolean containsCell(String value, String cssClass) {
        return html.selectContent(format("//td[@class='%s']", cssClass)).contains(value);
    }

}
