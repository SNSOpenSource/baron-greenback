package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaginatedHttpJobTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("moreXPath", "/root/more");
        context.put("checkpointAsString", "Today");
        context.put("checkpointXPath", "/root/date");
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.additionalWork(document("<root><date>Today</date></root>"));
        assertThat(more, is(none(PaginatedHttpJob.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("moreXPath", "/root/more");
        context.put("checkpointAsString", "Today");
        context.put("checkpointXPath", "/root/date");
        context.put("dataSource", HttpDataSource.dataSource(Uri.uri("http://go.away.com"), null));
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(context);
        Option<PaginatedHttpJob> more = job.additionalWork(document("<root><date>Yesterday</date><more>next</more></root>"));
        assertThat(more.get().dataSource().uri(), is(Uri.uri("next")));
    }

    @Test
    public void shouldHandleNoMore() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("moreXPath", "");
        assertThat(PaginatedHttpJob.paginatedHttpJob(context).additionalWork(null), is(none(PaginatedHttpJob.class)));
    }
}
