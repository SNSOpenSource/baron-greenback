package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.json.Json;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("batch")
public class BatchResource {

    private ModelRepository modelRepository;
    private Redirector redirector;

    public BatchResource(ModelRepository modelRepository, Redirector redirector) {
        this.modelRepository = modelRepository;
        this.redirector = redirector;
    }

    @GET
    @Path("import")
    public Model importForm() {
        return model();
    }

    @POST
    @Path("import")
    public Response importJson(@FormParam("model") String models) {
        Map<String,Object> uuidsAndModels = Json.parse(models);
        for(Map.Entry<String, Object> entry : uuidsAndModels.entrySet()) {
            modelRepository.set(UUID.fromString(entry.getKey()), Model.fromMap((Map<String, Object>) entry.getValue()));
        }

        return redirector.seeOther(method(on(BatchResource.class).importForm()));
    }
}
