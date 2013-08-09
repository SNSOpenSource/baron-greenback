package com.googlecode.barongreenback.search;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Link;

import static com.googlecode.barongreenback.shared.pager.Pager.ROWS_PER_PAGE_PARAM;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ViewSearchPage {
    private static final String NEXT_PAGE_XPATH = "//div[@class='pagination']/ul/li[@class='next page']/a";
    private final HttpHandler httpHandler;
    private final Html html;

    public ViewSearchPage(HttpHandler httpHandler, String view, int rowsPerPage) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(SearchResource.class).list(view, "")))).query(ROWS_PER_PAGE_PARAM, rowsPerPage).build()));
    }

    public ViewSearchPage(HttpHandler httpHandler, String view, String searchQuery) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(SearchResource.class).list(view, searchQuery)))).build()));
    }

    public ViewSearchPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Search"));
    }

    public boolean containsCell(String name, String value) {
        return html.selectContent(format("//td[@class='%s']", name)).contains(value);
    }

    public boolean containsCell(String name, int index, String value) {
        String xpath = format("//tbody/tr[%s]/td[@class='%s']", index + 1, name);
        String result = html.selectContent(xpath);
        return result.contains(value);
    }

    public Number resultsSize() {
        return html.count("//table[contains(@class, 'results')]/tbody/tr");
    }

    @Override
    public String toString() {
        return html.toString();
    }

    public ViewSearchPage nextPage() throws Exception {
        Link link = html.link(NEXT_PAGE_XPATH);
        return new ViewSearchPage(httpHandler, httpHandler.handle(link.click()));
    }

    public boolean hasNextPage() {
        return html.contains(NEXT_PAGE_XPATH);
    }
}
