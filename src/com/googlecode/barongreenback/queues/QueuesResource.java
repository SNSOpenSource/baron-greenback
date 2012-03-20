package com.googlecode.barongreenback.queues;

import com.googlecode.barongreenback.jobs.JobsResource;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Function1;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;

import static com.googlecode.barongreenback.queues.CompletedJob.completed;
import static com.googlecode.barongreenback.queues.RunningJob.started;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.MediaType.TEXT_PLAIN;
import static com.googlecode.utterlyidle.RequestBuilder.modify;

@Path("queues")
@Produces(TEXT_HTML)
public class QueuesResource {
    private final Queues queues;

    public QueuesResource(Queues queues) {
        this.queues = queues;
    }

    @GET
    @Path("list")
    public Model list() {
        return model().
                add("running", queues.running().sortBy(descending(started())).map(asRunningModel()).toList()).
                add("completed", queues.completed().sortBy(descending(completed())).map(asCompletedModel()).toList());
    }

    private Function1<RunningJob, Model> asRunningModel() {
        return new Function1<RunningJob, Model>() {
            @Override
            public Model call(RunningJob runningJob) throws Exception {
                return model().
                        add("status", "running").
                        add("started", runningJob.started).
                        add("completed", "").
                        add("duration", runningJob.duration()).
                        add("request", JobsResource.asModel(runningJob.request)).
                        add("response", "");
            }
        };
    }

    private Function1<CompletedJob, Model> asCompletedModel() {
        return new Function1<CompletedJob, Model>() {
            @Override
            public Model call(CompletedJob completedJob) throws Exception {
                return model().
                        add("status", "idle").
                        add("started", completedJob.started).
                        add("completed", completedJob.completed).
                        add("duration", completedJob.duration()).
                        add("request", JobsResource.asModel(completedJob.request)).
                        add("response", JobsResource.asModel(completedJob.response));
            }
        };
    }

    @POST
    @Path("queue")
    @Produces(TEXT_PLAIN)
    public Response queue(Request request, @PathParam("$") String endOfUrl) throws Exception {
        Request requestToQueue = modify(request).uri(request.uri().path(endOfUrl)).build();
        queues.queue(requestToQueue);
        return ResponseBuilder.response(Status.ACCEPTED.description("Queued Job")).entity("You Job has been accepted and is now in the queue").build();
    }
}