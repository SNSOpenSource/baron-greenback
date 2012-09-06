package com.googlecode.barongreenback.actions;

import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.barongreenback.search.SearchResource;
import com.googlecode.barongreenback.shared.AdvancedMode;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Strings;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import static com.googlecode.barongreenback.views.ViewsRepository.unwrap;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Produces(MediaType.TEXT_HTML)
@Path("{view}/actions")
public class ActionsResource {
    private final AdvancedMode mode;
    private final Redirector redirector;
    private final RecordsService recordsService;

    public ActionsResource(AdvancedMode mode, Redirector redirector, RecordsService recordsService) {
        this.mode = mode;
        this.redirector = redirector;
        this.recordsService = recordsService;
    }

    @POST
    @Path("delete")
    public Response delete(@PathParam("view") String viewName, @QueryParam("query") String query) {
        if (!mode.equals(AdvancedMode.Enable)) {
            return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
        }

        recordsService.delete(viewName, query);
        return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
    }


    @POST
    @Path("delete")
    public Response delete(@PathParam("view") String viewName, @QueryParam("query") String query, @QueryParam("id") Iterable<String> id) {
        String idName = unwrap(recordsService.view(viewName)).get("keywords", Model.class).get("name", String.class);
        delete(viewName, sequence(id).map(Strings.format(idName + ":%s")).toString(" OR "));
        return redirector.seeOther(method(on(SearchResource.class).list(viewName, query)));
    }


}
