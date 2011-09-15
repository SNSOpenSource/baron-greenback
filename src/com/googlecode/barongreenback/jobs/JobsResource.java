package com.googlecode.barongreenback.jobs;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.INITIAL_DELAY;
import static com.googlecode.barongreenback.jobs.HttpScheduler.INTERVAL;
import static com.googlecode.barongreenback.jobs.HttpScheduler.JOB_ID;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.barongreenback.jobs.HttpScheduler.TIME_UNIT;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.utterlyidle.proxy.Resource.redirect;
import static com.googlecode.utterlyidle.proxy.Resource.resource;

@Path("jobs")
public class JobsResource {
    private final HttpScheduler scheduler;
    private final Request request;

    public JobsResource(HttpScheduler scheduler, Request request) {
        this.scheduler = scheduler;
        this.request = request;
    }

    @POST
    @Path("schedule/{interval}")
    public Response schedule(@PathParam("interval") Long interval, @PathParam("$") String endOfUrl) throws Exception {
        Request scheduledRequest = request.uri(request.uri().path(endOfUrl));

        scheduler.schedule(scheduledRequest, record().set(INITIAL_DELAY, 0L).set(INTERVAL, interval).set(TIME_UNIT, TimeUnit.SECONDS));

        return redirectToList();
    }

    private Response redirectToList() {
        return redirect(resource(getClass()).list());
    }

    @POST
    @Path("reschedule")
    public Response reschedule(@FormParam("id") UUID id, @FormParam("interval") Long interval) throws Exception {
        scheduler.reschedule(id, record().set(INTERVAL, interval));
        return redirectToList();
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") UUID id) {
        // TODO: handle 404
        Record job = scheduler.job(id).get();
        return model().add("id", id.toString()).add("interval", job.get(INTERVAL));
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
        return model().add("jobs", jobsModel(scheduler.jobs()));
    }

    private List<Model> jobsModel(Sequence<Record> jobs) {
        return jobs.map(toModel()).toList();
    }

    private Callable1<? super Record, Model> toModel() {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                Request request = HttpMessageParser.parseRequest(record.get(REQUEST));
                return model().add("method", request.method()).add("path", request.uri().path()).add("delay", record.get(INTERVAL)).add("id", record.get(JOB_ID));
            }
        };
    }

}
