package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MoreDataCrawlerTest {
    @Test
    public void ifCheckpointFoundReturnNone() throws Exception {
        MoreDataCrawler crawler = new MoreDataCrawler();
        Option<Job> more = crawler.getMoreIfNeeded(Job.job(DataSource.dataSource(null, null, null, "/root/more", "Today", "/root/date"), null), document("<root><date>Today</date></root>"));
        assertThat(more, is(none(Job.class)));
    }

    @Test
    public void ifCheckpointNotFoundReturnNextJob() throws Exception {
        MoreDataCrawler crawler = new MoreDataCrawler();
        Option<Job> more = crawler.getMoreIfNeeded(Job.job(DataSource.dataSource(null, null, null, "/root/more", "Today", "/root/date"), null), document("<root><date>Yesterday</date><more>next</more></root>"));
        assertThat(more.get().dataSource().request().uri(), is(Uri.uri("next")));
    }
}
