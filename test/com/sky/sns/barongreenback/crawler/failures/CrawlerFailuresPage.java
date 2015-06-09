package com.sky.sns.barongreenback.crawler.failures;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.html.Browser;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.TableRow;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;

public class CrawlerFailuresPage {

    private final Browser browser;
    private Html html;

    public CrawlerFailuresPage(Browser browser) {
        this.browser = browser;
    }

    public void search(String query) throws Exception {
        final Request request = RequestBuilder.get(relativeUriOf(method(on(FailureResource.class).list(Option.<String>none(), query)))).build();
        html = Html.html(browser.handle(request));
    }

    public void deleteAll(String query) throws Exception {
        final Request request = RequestBuilder.post(relativeUriOf(method(on(FailureResource.class).deleteAll(query)))).build();
        html = Html.html(browser.handle(request));
    }

    public int getFailuresCount() {
        return html.table("//body[@class='failures']").bodyRows().size();
    }

    public Iterable<String> getFailureUrls() {
        return html.table("//body[@class='failures']").bodyRows().map(new Mapper<TableRow, String>() {
            @Override
            public String call(TableRow tableRow) throws Exception {
                return tableRow.cells().first().value();
            }
        });
    }

    public String getMessage(){
        return html.selectValues("//span[@class='message']").first();
    }
}
