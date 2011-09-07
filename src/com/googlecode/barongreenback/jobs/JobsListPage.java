package com.googlecode.barongreenback.jobs;

import com.googlecode.barongreenback.html.Html;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class JobsListPage {
    private final HttpHandler httpHandler;
    private final Html html;

    public JobsListPage(HttpHandler httpHandler, Response response) throws Exception {
        this.httpHandler = httpHandler;
        this.html = Html.html(response);
        assertThat(html.title(), containsString("Jobs"));
    }
}