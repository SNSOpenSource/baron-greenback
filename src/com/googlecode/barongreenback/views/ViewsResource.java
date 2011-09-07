package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static com.googlecode.barongreenback.shared.Forms.NUMBER_OF_FIELDS;
import static com.googlecode.barongreenback.shared.Forms.addTemplates;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.shared.RecordDefinition.toModel;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;
import static com.googlecode.utterlyidle.proxy.Resource.urlOf;

@Produces(MediaType.TEXT_HTML)
@Path("views")
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
    @Path("list")
    public Model list() {
        return menu("");
    }

    @GET
    @Path("new")
    public Model createForm() {
        return Forms.emptyForm(NUMBER_OF_FIELDS);
    }

    @POST
    @Path("new")
    public Response create(Model model) {
        return edit(model);
    }

    @GET
    @Path("edit")
    public Model editForm(@QueryParam("id") String name) {
        View view = views.get(name).get();
        return addTemplates(Forms.form(name, "", "", toModel(Keywords.keyword(""), view.fields())));
    }

    @POST
    @Path("edit")
    public Response edit(final Model root) {
        Model form = root.get("form", Model.class);
        String update = form.get("update", String.class);
        Model record = form.get("record", Model.class);
        RecordDefinition recordDefinition = convert(record);
        views.put(View.view(Keywords.keyword(update)).withFields(recordDefinition.fields()));
        return redirectToList();
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") String id) {
        views.remove(id);
        return redirectToList();
    }


    private Response redirectToList() {
        return redirect(resource(getClass()).list());
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
