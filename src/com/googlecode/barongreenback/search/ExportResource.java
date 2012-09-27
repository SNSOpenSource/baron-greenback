package com.googlecode.barongreenback.search;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("/export")
public class ExportResource {

    private final Redirector redirector;

    public ExportResource(Redirector redirector) {
        this.redirector = redirector;
    }

    @GET
    public Model export(@QueryParam("query") String query, @QueryParam("view") String view){
        Uri uri = redirector.uriOf(method(on(SearchResource.class).exportCsv(view, query)));
        return Model.model().add("csvUrl", uri);
    }
}
