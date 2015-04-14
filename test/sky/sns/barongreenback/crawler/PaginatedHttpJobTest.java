package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.jobs.PaginatedHttpJob;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.Minutes;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.barongreenback.crawler.datasources.HttpDataSource.httpDataSource;
import static com.googlecode.barongreenback.crawler.jobs.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.funclate.Model.persistent.model;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.Xml.document;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaginatedHttpJobTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        Model context = model().
                set("moreXPath", "/root/more").
                set("checkpoint", "Today").
                set("checkpointXPath", "/root/date");
        PaginatedHttpJob job = paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.nextPageJob(some(document("<root><date>Today</date><more>next</more></root>")));
        assertThat(more, is(none(PaginatedHttpJob.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        Model context = model().
                set("moreXPath", "/root/more").
                set("checkpoint", "Today").
                set("checkpointXPath", "/root/date").
                set("datasource", httpDataSource(uri("http://go.away.com"), null));
        PaginatedHttpJob job = paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.nextPageJob(some(document("<root><date>Yesterday</date><more>next</more></root>")));
        assertThat(more.get().dataSource().uri(), is(uri("next")));
    }

    @Test
    public void ifCheckpointIsDateBetweenExistingCheckpointsReturnNone() throws Exception {
        Date checkpoint = Dates.date(2013, 1, 1, 10, 30, 0);
        Date dateBefore = Minutes.subtract(checkpoint, 1);
        Date dateAfter = Minutes.add(checkpoint, 1);
        Model context = model().
                set("moreXPath", "/root/more").
                set("checkpoint", checkpoint).
                set("checkpointXPath", "/root/date");
        PaginatedHttpJob job = paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.nextPageJob(some(document(format("<root><date>%s</date><date>%s</date><more>next</more></root>",
                Dates.RFC3339withMilliseconds().format(dateBefore),
                Dates.RFC3339withMilliseconds().format(dateAfter)))));
        assertThat(more, is(none(PaginatedHttpJob.class)));
    }
}