package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.DefaultValue;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.Forms.NUMBER_OF_FIELDS;
import static com.googlecode.barongreenback.shared.Forms.addTemplates;
import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Produces(MediaType.TEXT_HTML)
@Path("views")
public class ViewsResource {
    private final Redirector redirector;
    private final ModelRepository modelRepository;

    public ViewsResource(Redirector redirector, ModelRepository modelRepository) {
        this.redirector = redirector;
        this.modelRepository = modelRepository;
    }

    @GET
    @Path("menu")
    public Model menu(@QueryParam("current") @DefaultValue("") String current) {
        return model().add("views", modelRepository.find(where(MODEL_TYPE, is("view"))).map(asModel(current)).toList());
    }

    private Callable1<? super Pair<UUID, Model>, Model> asModel(final String current) {
            return new Callable1<Pair<UUID, Model>, Model>() {
                public Model call(Pair<UUID, Model> pair) throws Exception {
                    Model model = pair.second().get("view");
                    return model.add("id", pair.first()).
                            add("current", current.equals(model.get("name"))).
                            add("url", redirector.uriOf(method(on(SearchResource.class).list("users", ""))));
                }
            };
        }

    @GET
    @Path("list")
    public Model list() {
        return menu("");
    }

    @GET
    @Path("new")
    public Model create() {
        return Forms.emptyForm(NUMBER_OF_FIELDS);
    }

    @POST
    @Path("new")
    public Response create(Model model) {
        return edit(UUID.randomUUID(), model);
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") UUID id) {
        return addTemplates(modelRepository.get(id).get());
    }

    @POST
    @Path("edit")
    public Response edit(@QueryParam("id") UUID id, final Model root) {
        modelRepository.set(id, clean(root));
        return redirectToList();
    }

    private Model clean(Model root) {
        Model record = root.get("view", Model.class);
        String name = record.get("name", String.class);
        return view(name, convert(record).toModel());
    }

    public static Model view(String update, Model definition) {
        return model().
                add("view", definition);
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id) {
        modelRepository.remove(id);
        return redirectToList();
    }


    private Response redirectToList() {
        return redirector.seeOther(method(on(getClass()).list()));
    }
}
