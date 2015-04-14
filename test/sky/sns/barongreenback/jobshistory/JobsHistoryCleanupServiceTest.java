package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.schedules.ScheduleListPage;
import com.googlecode.barongreenback.shared.ApplicationTests;
import org.junit.After;
import org.junit.Test;

import static com.googlecode.barongreenback.jobshistory.JobsHistoryResource.queryForOlderThan;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.relativeUriOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class JobsHistoryCleanupServiceTest extends ApplicationTests {

    @After
    public void stopApplication() throws Exception {
        application.stop();
    }

    @Test
    public void createsScheduleForDeletingOldJobHistoryItemsOnStartup() throws Exception {
        application.start();
        final ScheduleListPage schedulesPage = new ScheduleListPage(application);
        final String deleteUrl = "/jobs/create/" + relativeUriOf(method(on(JobsHistoryResource.class).remove(queryForOlderThan(JobHistoryItemLifespanInHours.DEFAULT)))).toString();
        assertThat(schedulesPage.jobUrls(), hasItem(deleteUrl));
    }
}