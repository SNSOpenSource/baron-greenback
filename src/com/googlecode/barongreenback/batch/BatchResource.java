package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.json.Json;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Path("batch")
@Produces(MediaType.TEXT_HTML)
public class BatchResource {

    private ModelRepository modelRepository;
    private Redirector redirector;

    public BatchResource(ModelRepository modelRepository, Redirector redirector) {
        this.modelRepository = modelRepository;
        this.redirector = redirector;
    }

    @GET
    @Path("import")
    public Model operations() {
        return model();
    }

    @GET
    @Path("import")
    public Model operations(@QueryParam("message") String message, @QueryParam("category") String category) {
        return messageModel(message, category);
    }

    @GET
    @Path("export")
    @Produces("application/json")
    public String export() {
        Map<String, Object> map = modelRepository.find(Predicates.<Record>all()).fold(new HashMap<String, Object>(), addUuidAndModel());
        return Json.toJson(map);
    }

    @POST
    @Path("import")
    public Object importJson(@FormParam("model") String batchModel) {
        try {
            Map<String, Object> uuidsAndModels = Json.parse(batchModel);
            for (Map.Entry<String, Object> entry : uuidsAndModels.entrySet()) {
                modelRepository.set(UUID.fromString(entry.getKey()), Model.fromMap((Map<String, Object>) entry.getValue()));
            }
            return redirectWithMessage(String.format("Imported %s items", uuidsAndModels.size()), "success");
        } catch (Exception e) {
            return messageModel(String.format("Import error: %s", e.getMessage()), "error").add("model", batchModel);
        }

    }

    private Model messageModel(String message, String category) {
        return model().add("message", model().add("text", message).add("category", category));
    }

    private Response redirectWithMessage(String text, String category) {
        return redirector.seeOther(method(on(BatchResource.class).operations(text, category)));
    }

    private Callable2<? super Map<String, Object>, ? super Pair<UUID, Model>, Map<String, Object>> addUuidAndModel() {
        return new Callable2<Map<String, Object>, Pair<UUID, Model>, Map<String, Object>>() {
            public Map<String, Object> call(Map<String, Object> map, Pair<UUID, Model> pair) throws Exception {
                map.put(pair.first().toString(), pair.second());
                return map;
            }
        };
    }
}
