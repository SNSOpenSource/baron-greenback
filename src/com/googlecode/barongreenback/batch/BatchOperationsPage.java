package com.googlecode.barongreenback.batch;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class BatchOperationsPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public BatchOperationsPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(BatchResource.class).operations()))).build()));
    }

    public BatchOperationsPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Batch Operations"));
    }

    public void delete() throws Exception {
        httpHandler.handle(html.form("//form[contains(@class, 'delete')]").submit("descendant::input[contains(@class, 'delete')]"));
    }

    public void backup(String location) throws Exception {
        html.input("descendant::input[contains(@class, 'location')]").value(location);
        httpHandler.handle(html.form("//form[contains(@class, 'backup')]").submit("descendant::input[contains(@class, 'backup')]"));
    }
}
