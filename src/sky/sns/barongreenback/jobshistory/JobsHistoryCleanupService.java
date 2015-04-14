package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.schedules.ScheduleResource;
import com.googlecode.utterlyidle.services.StartOnlyService;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.barongreenback.jobshistory.JobsHistoryResource.queryForOlderThan;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.RequestBuilder.post;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static java.util.concurrent.TimeUnit.DAYS;

public class JobsHistoryCleanupService extends StartOnlyService {

    private static final UUID CLEANUP_JOBS_HISTORY_ID = UUID.fromString("c195087c-e009-11e3-b7a0-82687f4fc15c");
    private static final String START_TIME = "0100";

    private final JobHistoryItemLifespanInHours jobHistoryItemLifespanInHours;
    private final Application application;

    public JobsHistoryCleanupService(JobHistoryItemLifespanInHours jobHistoryItemLifespanInHours, Application application) {
        this.jobHistoryItemLifespanInHours = jobHistoryItemLifespanInHours;
        this.application = application;
    }

    @Override
    public void start() throws Exception {
        scheduleDeletion();
    }

    private Response scheduleDeletion() throws Exception {
        return application.usingRequestScope(new Function1<Container, Response>() {
            @Override
            public Response call(Container container) throws Exception {
                Uri uri = relativeUriOf(method(on(ScheduleResource.class).scheduleWithQueryParams(CLEANUP_JOBS_HISTORY_ID, some(START_TIME), DAYS.toSeconds(1), deleteOldJobHistoryItems())));
                return application.handle(post(uri).build());
            }
        });
    }

    private String deleteOldJobHistoryItems() {
        return "/jobs/create/" + relativeUriOf(method(on(JobsHistoryResource.class).remove(queryForOlderThan(jobHistoryItemLifespanInHours.value())))).toString();
    }
}
