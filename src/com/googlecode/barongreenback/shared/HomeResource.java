package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.crawler.CrawlerResource;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

public class HomeResource {
    private final Redirector redirector;

    public HomeResource(Redirector redirector) {
        this.redirector = redirector;
    }

    @GET
    @Path("")
    public Response homePage() {
        return redirector.seeOther(method(on(CrawlerResource.class).list()));
    }
}
