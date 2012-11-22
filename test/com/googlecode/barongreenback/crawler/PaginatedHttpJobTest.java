package com.googlecode.barongreenback.crawler;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Option;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.HttpDatasource.httpDatasource;
import static com.googlecode.barongreenback.crawler.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.funclate.Model.immutable.model;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaginatedHttpJobTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        Model context = model().
                set("moreXPath", "/root/more").
                set("checkpointAsString", "Today").
                set("checkpointXPath", "/root/date");
        PaginatedHttpJob job = paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.nextPageJob(some(document("<root><date>Today</date></root>")));
        assertThat(more, is(none(PaginatedHttpJob.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        Model context = model().
                set("moreXPath", "/root/more").
                set("checkpointAsString", "Today").
                set("checkpointXPath", "/root/date").
                set("datasource", httpDatasource(uri("http://go.away.com"), null));
        PaginatedHttpJob job = paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.nextPageJob(some(document("<root><date>Yesterday</date><more>next</more></root>")));
        assertThat(more.get().datasource().uri(), is(uri("next")));
    }
}