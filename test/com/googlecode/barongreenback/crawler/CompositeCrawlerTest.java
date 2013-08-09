package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.ATOM_DEFINITION;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.FIRST;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.ID;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.USER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompositeCrawlerTest extends CrawlerTests {
    @Test
    public void shouldGetTheContentsOfAUrlAndExtractContent() throws Exception {
        Sequence<Record> records = crawl(feedClient(), feed());
        Record entry = records.head();

        assertThat(entry.get(ID), is("urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1"));
        assertThat(entry.get(USER_ID), is(1234));
        assertThat(entry.get(FIRST), is("Dan"));
    }

    @Test
    public void shouldNotGoPastTheCheckpoint_checkpointValue() throws Exception {
        Sequence<Record> records = crawl(feedClient(), "2011-07-19T12:43:25.000Z", feed());
        assertThat(records.size(), Matchers.<Number>is(1));
    }

    public static Sequence<Record> crawl(HttpClient httpClient, Uri feed) throws Exception {
        return crawl(httpClient, null, feed);
    }

    public static Sequence<Record> crawlOnePageOnly(Uri feed, HttpClient httpClient) throws Exception {
        return crawler(httpClient).crawl(feed, "", null, ATOM_DEFINITION);
    }

    private static CompositeCrawler crawler(HttpClient httpClient) {
        return new CompositeCrawler(httpClient, System.out);
    }

    public static Sequence<Record> crawl(HttpClient httpClient, Object checkpoint, Uri feed) throws Exception {
        return crawler(httpClient).crawl(feed, "/feed/link/@href", checkpoint, ATOM_DEFINITION);
    }

}
