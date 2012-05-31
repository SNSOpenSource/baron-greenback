package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaginatedHttpJobTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(null, null, null, null, "/root/more", "Today", "/root/date");
        Option<PaginatedHttpJob> more = job.additionalWork(null, document("<root><date>Today</date></root>"));
        assertThat(more, is(none(PaginatedHttpJob.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(HttpDataSource.dataSource(Uri.uri("http://go.away.com"), null), null, null, null, "/root/more", "Today", "/root/date");
        Option<PaginatedHttpJob> more = job.additionalWork(null, document("<root><date>Yesterday</date><more>next</more></root>"));
        assertThat(more.get().dataSource().uri(), is(Uri.uri("next")));
    }

    @Test
    public void shouldHandleNoMore() throws Exception {
        assertThat(PaginatedHttpJob.paginatedHttpJob(null, null, null, null, "", null, (String)null).additionalWork(null, null), is(none(PaginatedHttpJob.class)));
    }
}
