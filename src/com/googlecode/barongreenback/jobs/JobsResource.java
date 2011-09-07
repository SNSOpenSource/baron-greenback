package com.googlecode.barongreenback.jobs;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.HttpMessageParser;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.RequestBuilder;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Responses;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.annotations.DELETE;
import com.googlecode.utterlyidle.annotations.GET;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.PUT;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.PathParam;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.googlecode.barongreenback.jobs.HttpScheduler.DELAY;
import static com.googlecode.barongreenback.jobs.HttpScheduler.INITIAL_DELAY;
import static com.googlecode.barongreenback.jobs.HttpScheduler.REQUEST;
import static com.googlecode.barongreenback.jobs.HttpScheduler.TIME_UNIT;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.utterlyidle.AbsoluteLocationHandler.toAbsoluteUri;

@Path("jobs")
public class JobsResource {
    private final HttpScheduler scheduler;
    private final Request request;
    private final BasePath basePath;

    public JobsResource(HttpScheduler scheduler, Request request, BasePath basePath) {
        this.scheduler = scheduler;
        this.request = request;
        this.basePath = basePath;
    }

    // http://localhost:9000/jobs/schedule/5/crawler/crawl/?id=73773c20-5bf8-4da1-865b-c44c56656c47
    @POST
    @Path("schedule/{interval}")
    public Response schedule(@PathParam("interval") Long interval, @PathParam("$") String endOfUrl) throws Exception {
        Request scheduledRequest = request.uri(request.uri().path(endOfUrl));

        scheduler.schedule(scheduledRequest, record().set(INITIAL_DELAY, 0L).set(DELAY, interval).set(TIME_UNIT, TimeUnit.SECONDS));

        return Responses.response(Status.OK).entity("scheduled");
    }

    @GET
    @Path("list")
    public Model list() {
        return model().add("jobs", jobsModel(scheduler.jobs()));
    }

//    @POST
//    @Path("queue/{method}/{uri:.+}")
//    public Response queue(@PathParam("method") String httpMethod, @PathParam("uri") String uri) throws Exception {
//        Request oneOffRequest = new RequestBuilder(httpMethod, toAbsoluteUri(uri, request, basePath).toString()).build();
//
//        Executors.newSingleThreadExecutor().submit(httpTask(oneOffRequest));
//
//        return Responses.response(Status.OK).entity("queued");
//    }

    private List<Model> jobsModel(Sequence<Record> jobs) {
        return jobs.map(toModel()).toList();
    }

    private Callable1<? super Record, Model> toModel() {
        return new Callable1<Record, Model>() {
            public Model call(Record record) throws Exception {
                Request request = HttpMessageParser.parseRequest(record.get(REQUEST));
                return model().add("method", request.method()).add("path", request.uri().path()).add("delay", record.get(DELAY));
            }
        };
    }

}
