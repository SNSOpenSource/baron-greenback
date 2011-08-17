package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.WebApplication;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;
import org.junit.Test;

import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ViewsResourceTest {
    @Test
    public void displaysAListOfViews() throws Exception {
        Response response = application(new WebApplication()).handle(get("views/menu"));
        System.out.println(response);
        assertThat(response.status(), is(Status.OK));
    }
}
