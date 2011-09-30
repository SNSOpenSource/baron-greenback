package com.googlecode.barongreenback.jobs;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.FormParam;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;
import com.googlecode.utterlyidle.annotations.QueryParam;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.INTERVAL;
import static com.googlecode.barongreenback.jobs.HttpScheduler.JOB_ID;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.totallylazy.records.MapRecord.record;

@Path("jobs")
public class JobsResource {
    public static final Long DEFAULT_INTERVAL = 30L;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private final HttpScheduler scheduler;
    private final Request request;
    private final Redirector redirector;

    public JobsResource(HttpScheduler scheduler, Request request, Redirector redirector) {
        this.scheduler = scheduler;
        this.request = request;
        this.redirector = redirector;
    }

    @POST
    @Path("schedule/{id}/{interval}")
    public Response schedule(@PathParam("id") String id, @PathParam("interval") Long interval, @PathParam("$") String endOfUrl) throws Exception {
        Request scheduledRequest = request.uri(request.uri().path(endOfUrl));

        scheduler.schedule(record().set(INTERVAL, interval).set(JOB_ID, id).set(REQUEST, scheduledRequest.toString()));

        return redirectToList();
    }

    @POST
    @Path("reschedule")
    public Response reschedule(@FormParam("id") String id, @FormParam("interval") Long interval) throws Exception {
        scheduler.schedule(record().set(INTERVAL, interval).set(JOB_ID, id));
        return redirectToList();
    }

    @GET
    @Path("edit")
    public Model edit(@QueryParam("id") String id) {
        // TODO: handle 404
        Record job = scheduler.job(id).get();
        return model().add("id", id.toString()).add("interval", job.get(INTERVAL));
    }

    @POST
    @Path("delete")
    public Response delete(@FormParam("id") String id) {
        scheduler.remove(id);
        return redirectToList();
    }

    @GET
    @Path("list")
    public Model list() {
        return model().add("jobs", jobsModel(scheduler.jobs()));
    }

    @POST
    @Path("start")
    public Response resumeAll() {
        scheduler.resumeAll();
        return redirector.seeOther(method(on(JobsResource.class).list()));
    }

    @POST
    @Path("stop")
    public Response pauseAll() {
        scheduler.pauseAll();
        return redirector.seeOther(method(on(JobsResource.class).list()));
    }

    private Response redirectToList() {
        return redirector.seeOther(method(on(getClass()).list()));
    }

    private List<Model> jobsModel(Sequence<Record> jobs) {
        return jobs.map(toModel()).toList();
    }

    private Callable1<? super Record, Model> toModel() {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                return model().add("delay", record.get(INTERVAL)).
                               add("id", record.get(JOB_ID));
            }
        };
    }

}
