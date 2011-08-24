package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.crawler.Crawler;
import com.googlecode.barongreenback.crawler.CrawlerResource;
import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;

import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

public class HomeResource {
    @GET
    @Path("")
    public Response homePage() {
        return redirect(resource(CrawlerResource.class).get(Integer.parseInt(CrawlerResource.NUMBER_OF_FIELDS)));
    }
}
