package com.googlecode.barongreenback.search;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.TableRow;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.TITLE;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

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

    private static RequestBuilder url(String view, String query) {
        return get("/" + relativeUriOf(method(on(SearchResource.class).list(view, query)))).query("decorator", "none");
    }

    public Number numberOfResults() {
        return html.count("//table[contains(@class, 'results')]/tbody/tr");
    }

    public SearchPage delete() throws Exception {
        Request request = html.form("//form[contains(@class, 'delete')]").submit("descendant::input[@type='submit' and @class='delete']");
        Response response = httpHandler.handle(request);
        return new SearchPage(httpHandler, response);
    }

    public String queryMessage() {
        return html.selectContent("//div[contains(@class, 'error')]/span[contains(@class, 'message')]");
    }

    public String exportToCsv(String view, String query) throws Exception {
        Response response = httpHandler.handle(get("/" + relativeUriOf(method(on(SearchResource.class).exportCsv(view, query)))).build());
        assertThat(response.status(), is(Status.OK));
        return response.entity().toString();
    }

    public Sequence<Record> displayedEntries() {
        return html.table("//table[contains(@class, 'results')]", "descendant::text()").bodyRows().map(new Callable1<TableRow, Record>() {
            @Override
            public Record call(TableRow tableRow) throws Exception {
                return record().set(TITLE, tableRow.get(TITLE.name()).value());
            }
        });
    }
}