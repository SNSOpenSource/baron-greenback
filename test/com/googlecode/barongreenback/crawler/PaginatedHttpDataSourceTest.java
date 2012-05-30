package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.PaginatedHttpDataSource.*;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaginatedHttpDataSourceTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        PaginatedHttpDataSource dataSource = dataSource(null, null, null, "/root/more", "Today", "/root/date");
        Option<HttpJob> more = dataSource.additionalWork(null, document("<root><date>Today</date></root>"));
        assertThat(more, is(none(HttpJob.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        PaginatedHttpDataSource dataSource = dataSource(null, null, null, "/root/more", "Today", "/root/date");
        Option<HttpJob> more = dataSource.additionalWork(null, document("<root><date>Yesterday</date><more>next</more></root>"));
        assertThat(more.get().dataSource().uri(), is(Uri.uri("next")));
    }

    @Test
    public void shouldHandleNoMore() throws Exception {
        assertThat(dataSource(null, null, null, "", null, null).additionalWork(null, null), is(none(HttpJob.class)));
    }
}
