package com.googlecode.barongreenback.jobs;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Redirector;
import com.googlecode.utterlyidle.RequestGenerator;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.annotations.POST;
import com.googlecode.utterlyidle.annotations.Path;
import com.googlecode.utterlyidle.annotations.Produces;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.BatchCrawlerResource.forAll;
import static com.googlecode.barongreenback.jobs.Job.asJobId;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;

@Path("jobs")
@Produces(TEXT_HTML)
public class BatchJobsResource {

    private Application application;
    private RequestGenerator requestGenerator;
    private Jobs jobs;
    private HttpScheduler scheduler;
    private Redirector redirector;

    public BatchJobsResource(final Application application, final RequestGenerator requestGenerator, final Jobs jobs, final HttpScheduler scheduler, final Redirector redirector) {
        this.application = application;
        this.requestGenerator = requestGenerator;
        this.jobs = jobs;
        this.scheduler = scheduler;
        this.redirector = redirector;
    }

    @POST
    @Path("start")
    public Response start() {
        scheduler.start();
        return redirector.seeOther(method(on(JobsResource.class).list()));
    }

    @POST
    @Path("stop")
    public Response stop() {
        scheduler.stop();
        return redirector.seeOther(method(on(JobsResource.class).list()));
    }

    @POST
    @Path("deleteAll")
    public Response deleteAll() throws Exception {
        return forAll(ids(), delete());
    }

    public Callable1<UUID, Response> delete() {
        return new Callable1<UUID, Response>() {
            public Response call(UUID uuid) throws Exception {
                return application.handle(requestGenerator.requestFor(method(on(JobsResource.class).delete(uuid))));
            }
        };
    }

    private Sequence<UUID> ids() {
        return jobs.jobs().map(asJobId());
    }

}
