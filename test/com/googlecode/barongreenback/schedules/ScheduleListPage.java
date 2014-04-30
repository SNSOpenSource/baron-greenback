package com.googlecode.barongreenback.schedules;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.html.Html;
import com.googlecode.utterlyidle.schedules.ScheduleResource;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ScheduleListPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public ScheduleListPage(HttpHandler httpHandler) throws Exception {
        this(httpHandler, httpHandler.handle(get("/" + relativeUriOf(method(on(ScheduleResource.class).list()))).build()));
    }

    public ScheduleListPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Schedules"));
    }

    public int numberOfJobs() {
        return html.count("//tr[@class='job']").intValue();
    }
}