package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.Forms;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Second;
import com.googlecode.totallylazy.comparators.Comparators;
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

import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.shared.Forms.NUMBER_OF_FIELDS;
import static com.googlecode.barongreenback.shared.Forms.addTemplates;
import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.barongreenback.views.Views.clean;
import static com.googlecode.barongreenback.views.Views.name;
import static com.googlecode.barongreenback.views.Views.priority;
import static com.googlecode.barongreenback.views.Views.valueFor;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.ascending;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static java.util.UUID.randomUUID;

@Produces(MediaType.TEXT_HTML)
@Path("views")
public class ViewsResource {
    private final Redirector redirector;
    private final ModelRepository modelRepository;
    private final RecordsService recordsService;

    public ViewsResource(Redirector redirector, ModelRepository modelRepository, RecordsService recordsService) {
        this.redirector = redirector;
        this.modelRepository = modelRepository;
        this.recordsService = recordsService;
    }

    @GET
    @Path("menu")
    public Model menu(@QueryParam("current") @DefaultValue("") String current) {
        return models(current, Views.where(valueFor("visible", Boolean.class), is(true)));
    }

    private Model models(String current, Predicate<Second<Model>> predicate) {
        List<Model> models = modelRepository.
                find(Predicates.where(MODEL_TYPE, is("view"))).
                filter(predicate).
                map(asModel(current)).
                sortBy(Comparators.comparators(ascending(priority()), ascending(name()))).
                toList();
        return model().add("views", models).add("anyExists", !models.isEmpty());
    }

    @GET
    @Path("list")
    public Model list() {
        return models("", Predicates.<Second<Model>>all());
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

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id) {
        modelRepository.remove(id);
        return redirectToList();
    }

    @POST
    @Path("copy")
    public Response copy(@FormParam("id") UUID id) {
        Option<Model> modelOption = modelRepository.get(id);
        Model modelCopy = Views.copy(modelOption.get());
        modelRepository.set(UUID.randomUUID(), modelCopy);
        return redirectToList();
    }

    @GET
    @Path("export")
    @Produces("application/json")
    public String export(@QueryParam("id") UUID id) {
        return modelRepository.get(id).get().toString();
    }

    @GET
    @Path("exists")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean exists(@QueryParam("id") UUID id) {
        return !modelRepository.get(id).isEmpty();
    }

    @GET
    @Path("import")
    public Model importForm() {
        return model();
    }

    @POST
    @Path("import")
    public Response importJson(@FormParam("model") String model, @FormParam("id") Option<UUID> id) {
        modelRepository.set(id.getOrElse(randomUUID()), Model.parse(model));
        return redirectToList();
    }

    private Response redirectToList() {
        return redirector.seeOther(method(on(getClass()).list()));
    }

    private Callable1<? super Pair<UUID, Model>, Model> asModel(final String current) {
        return new Callable1<Pair<UUID, Model>, Model>() {
            public Model call(Pair<UUID, Model> pair) throws Exception {
                Model model = pair.second().get("view");
                String name = model.get("name", String.class);
                return model.add("id", pair.first()).
                        add("current", current.equals(name)).
                        add("itemsTotal", recordsService.count(model.<String>get("records"), model.<String>get("query"))).
                        add("url", redirector.uriOf(method(on(SearchResource.class).list(name, ""))));
            }
        };
    }
}
