package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.jobs.HttpScheduler;
import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.queues.Queues;
import com.googlecode.barongreenback.shared.ModelCache;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.messages.Category;
import com.googlecode.barongreenback.shared.messages.Messages;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.json.Json;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.barongreenback.shared.messages.Messages.error;
import static com.googlecode.barongreenback.shared.messages.Messages.success;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static java.lang.String.format;

@Path("batch")
@Produces(MediaType.TEXT_HTML)
public class BatchResource {

    private ModelRepository modelRepository;
    private Redirector redirector;
    private final Persistence persistence;
    private final HttpScheduler scheduler;
    private final Queues queues;
    private final ModelCache cache;

    public BatchResource(final ModelRepository modelRepository, final Redirector redirector, final Persistence persistence, final HttpScheduler scheduler, final Queues queues, final ModelCache cache) {
        this.modelRepository = modelRepository;
        this.redirector = redirector;
        this.persistence = persistence;
        this.scheduler = scheduler;
        this.queues = queues;
        this.cache = cache;
    }

    @GET
    @Path("operations")
    public Model operations() {
        return model();
    }

    @GET
    @Path("operations")
    public Model operations(@QueryParam("message") String message, @QueryParam("category") Category category) {
        return Messages.messageModel(message, category);
    }

    @GET
    @Path("import")
    public Model batchImport(@QueryParam("message") String message, @QueryParam("category") Category category) {
        return Messages.messageModel(message, category);
    }

    @GET
    @Path("import")
    public Model batchImport() {
        return model();
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
            return success(format("Imported %s items", uuidsAndModels.size()));
        } catch (Exception e) {
            return error(format("Import error: %s", e.getMessage())).add("model", batchModel);
        }

    }

    @POST
    @Path("delete")
    public Object deleteIndex() throws IOException {
        try {
            cache.clear();
            scheduler.stop();
            persistence.deleteAll();
            queues.deleteAll();
            return redirector.seeOther(method(on(BatchResource.class).operations("Index has been deleted and all pending jobs stopped", Category.SUCCESS)));
        } catch(Exception e) {
            return redirector.seeOther(method(on(BatchResource.class).operations("Error occurred when deleting the index: " + e.getMessage(), Category.ERROR)));
        }
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
