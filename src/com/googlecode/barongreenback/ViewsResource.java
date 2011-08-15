package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static com.googlecode.barongreenback.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;

@Produces(MediaType.TEXT_HTML)
public class ViewsResource {
    private final Views views;

    public ViewsResource(Views views) {
        this.views = views;
    }

    @GET
    @Path("menu")
    public Model menu(@QueryParam("current") @DefaultValue("") String current) {
        return model().add("views", views.get().map(asModel(current)).toList());
    }

    @GET
    @Path("view")
    public Model createForm() {
        return model();
    }

    @GET
    @Path("views")
    public Model list() {
        return menu("");
    }

    @POST
    @Path("view")
    public Model create(RecordDefinition recordDefinition) {
        views.add(view(recordDefinition.recordName()).withFields(recordDefinition.fields()));
        return model();
    }

    private Callable1<? super View, Model> asModel(final String current) {
        return new Callable1<View, Model>() {
            public Model call(View view) throws Exception {
                Keyword keyword = view.name();
                return model().
                        add("current", keyword.name().equalsIgnoreCase(current)).
                        add("name", keyword.name()).
                        add("url", urlOf(resource(SearchResource.class).find(keyword.name(), "")));
            }
        };
    }
}
