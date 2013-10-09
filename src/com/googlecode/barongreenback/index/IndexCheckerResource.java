package com.googlecode.barongreenback.index;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.io.IOException;

import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;

@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
@Path("index")
public class IndexCheckerResource {
    private final PartitionedIndex partitionedindex;
    private final Redirector redirector;

    public IndexCheckerResource(PartitionedIndex partitionedindex, Redirector redirector) {
        this.partitionedindex = partitionedindex;
        this.redirector = redirector;
    }

    public IndexCheckerResource(BaronGreenbackRequestScope baronGreenbackRequestScope, Redirector redirector) {
        this(baronGreenbackRequestScope.value().get(PartitionedIndex.class), redirector);
    }

    @GET
    @Path("check")
    public Model check() {
        return model().add("items", partitionedindex.partitions().toSequence().mapConcurrently(new Mapper<Pair<String, LuceneStorage>, Model>() {
            @Override
            public Model call(Pair<String, LuceneStorage> pair) throws Exception {
                return model().
                        add("name", pair.first()).
                        add("status", pair.second().check().clean);
            }
        }));
    }

    @POST
    @Path("fix")
    public Response fix(@FormParam("id") String id) throws IOException {
        partitionedindex.partitions().lookup(id).get().fix();
        return redirector.seeOther(method(on(IndexCheckerResource.class).check()));
    }

    @POST
    @Path("fixAll")
    public Response fixAll() throws IOException {
        partitionedindex.partitions().toSequence().eachConcurrently(new Block<Pair<String, LuceneStorage>>() {
            @Override
            protected void execute(Pair<String, LuceneStorage> pair) throws Exception {
                pair.second().fix();
            }
        });
        return redirector.seeOther(method(on(IndexCheckerResource.class).check()));
    }
}
