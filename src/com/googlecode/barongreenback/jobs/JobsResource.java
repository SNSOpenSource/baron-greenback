package com.googlecode.barongreenback.jobs;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.Produces;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.jobs.Job.COMPLETED;
import static com.googlecode.barongreenback.jobs.Job.DURATION;
import static com.googlecode.barongreenback.jobs.Job.INTERVAL;
import static com.googlecode.barongreenback.jobs.Job.JOB_ID;
import static com.googlecode.barongreenback.jobs.Job.REQUEST;
import static com.googlecode.barongreenback.jobs.Job.RESPONSE;
import static com.googlecode.barongreenback.jobs.Job.RUNNING;
import static com.googlecode.barongreenback.jobs.Job.STARTED;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.RequestBuilder.modify;

@Path("jobs")
@Produces(TEXT_HTML)
public class JobsResource {
    public static final Long DEFAULT_INTERVAL = 600L;
    private final HttpScheduler scheduler;
    private final Request request;
    private final Redirector redirector;

    public JobsResource(HttpScheduler scheduler, Request request, Redirector redirector) {
        this.scheduler = scheduler;
        this.request = request;
        this.redirector = redirector;
    }

    @POST
    @Path("schedule/{id}/{seconds}")
    public Response schedule(@PathParam("id") UUID id, @PathParam("seconds") Long seconds, @PathParam("$") String endOfUrl) throws Exception {
        Request scheduledRequest = modify(request).uri(request.uri().path(endOfUrl)).build();

        scheduler.schedule(Job.job(id).interval(seconds).request(scheduledRequest.toString()));

        return redirectToList();
    }

    @POST
    @Path("reschedule")
    public Response reschedule(@FormParam("id") UUID id, @FormParam("seconds") Long seconds) throws Exception {
        scheduler.schedule(Job.job(id).interval(seconds));
        return redirectToList();
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") UUID id) {
        Record job = scheduler.job(id).get();
        return model().add("id", id.toString()).add("seconds", job.get(INTERVAL));
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") UUID id) {
        scheduler.remove(id);
        return redirectToList();
    }

    @GET
    @Path("list")
    public Model list() {
        List<Model> models = jobsModel(scheduler.jobs());
        return model().add("jobs", models).add("anyExists", !models.isEmpty());
    }

    private Response redirectToList() {
        return redirector.seeOther(method(on(getClass()).list()));
    }

    public static List<Model> jobsModel(Sequence<Record> jobs) {
        return jobs.map(toModel()).toList();
    }

    public static Callable1<? super Record, Model> toModel() {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                return model().
                        add("status", Boolean.TRUE.equals(record.get(RUNNING))? "running" : "idle").
                        add("id", record.get(JOB_ID)).
                        add("request", addRequest(record)).
                        add("response", addResponse(record)).
                        add("seconds", record.get(INTERVAL)).
                        add("started", record.get(STARTED)).
                        add("completed", record.get(COMPLETED)).
                        add("duration", record.get(DURATION));
            }
        };
    }

    public static Model addRequest(Record record) {
        String requestMessage = record.get(REQUEST);
        if(requestMessage == null){
            return null;
        }
        Request request = HttpMessageParser.parseRequest(requestMessage);
        return model().
                add("raw", requestMessage).
                add("method", request.method()).
                add("uri", request.uri()).
                add("entity", new String(request.entity()));
    }

    public static Model addResponse(Record record) {
        String responseMessage = record.get(RESPONSE);
        if(responseMessage == null){
            return null;
        }
        Response response = HttpMessageParser.parseResponse(responseMessage);
        return model().
                add("raw", responseMessage).
                add("code", response.status().code()).
                add("status", response.status().description()).
                add("entity", new String(response.bytes()));
    }


}
