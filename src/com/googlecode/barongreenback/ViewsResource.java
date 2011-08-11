package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import static com.googlecode.barongreenback.View.view;
import static com.googlecode.totallylazy.records.Keywords.keyword;

@Path("views")
@Produces(MediaType.TEXT_HTML)
public class ViewsResource {
    private final Views views;

    public ViewsResource(Views views) {
        this.views = views;
    }

    @GET
    @Path("list")
    public Model list(){
        return Model.model().add("views", views.get().toList());
    }
}
