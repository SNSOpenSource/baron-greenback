package com.googlecode.barongreenback.views;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.html.Link;

import java.util.UUID;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ViewListPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public ViewListPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(ViewsResource.class).list()))).build()));
    }

    public ViewListPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Views"));
    }

    public Link link(String value) {
        return html.link("//a[contains(@class, 'name') and text() = '" + value + "']");
    }

    public ViewEditPage edit(UUID id) throws Exception {
        Request request = html.link(String.format("//a[contains(@class, 'edit') and text() = 'edit' and @href='edit?id=%s']", id)).click();
        Response response = httpHandler.handle(request);
        return new ViewEditPage(httpHandler, response);
    }

    public Number count() {
        return html.count("//table[contains(@class, 'results')]/tbody/tr");
    }

    public ViewListPage delete(UUID id) throws Exception {
        Request request = html.form(String.format("//form[@class='delete' and input[@name='id' and @value='%s']]", id)).submit("input[@value='delete']");
        Response response = httpHandler.handle(request);
        return new ViewListPage(httpHandler, response);
    }
}