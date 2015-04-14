package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.handlers.InvocationHandler;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.BatchCrawlerResource.forAll;
import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
@Path("views")
public class BatchViewsResource {
    private final ModelRepository modelRepository;
    private InvocationHandler invocationHandler;

    public BatchViewsResource(final InvocationHandler invocationHandler, ModelRepository modelRepository) {
        this.invocationHandler = invocationHandler;
        this.modelRepository = modelRepository;
    }

    @POST
    @Path("deleteAll")
    public Response deleteAll() throws Exception {
        return forAll(ids(), delete()).get();
    }

    private Sequence<UUID> ids() {
        return allViewsModels().map(first(UUID.class));
    }

    public Callable1<UUID, Response> delete() {
        return new Callable1<UUID, Response>() {
            public Response call(UUID uuid) throws Exception {
                return invocationHandler.handle(method(on(ViewsResource.class).delete(uuid)));
            }
        };
    }

    private Sequence<Pair<UUID, Model>> allViewsModels() {
        return modelRepository.find(where(MODEL_TYPE, is("view")));
    }
}
