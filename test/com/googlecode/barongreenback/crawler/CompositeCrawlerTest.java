package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.*;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompositeCrawlerTest extends CrawlerTests {
    @Test
    public void shouldGetTheContentsOfAUrlAndExtractContent() throws Exception {
        Sequence<Record> records = crawl();
        Record entry = records.head();

        assertThat(entry.get(ID), is("urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1"));
        assertThat(entry.get(USER_ID), is(1234));
        assertThat(entry.get(FIRST), is("Dan"));
    }

    @Test
    public void shouldNotGoPastTheCheckpoint_checkpointValue() throws Exception {
        Sequence<Record> records = crawl("2011-07-19T12:43:25.000Z");
        assertThat(records.size(), Matchers.<Number>is(1));
    }

    public static Sequence<Record> crawl() throws Exception {
        return crawl(null);
    }

    public static Sequence<Record> crawlOnePageOnly() throws Exception {
        return new CompositeCrawler().crawl(atomXml, "", null, ATOM_DEFINITION);
    }

    public static Sequence<Record> crawl(Object checkpoint) throws Exception {
        return new CompositeCrawler().crawl(atomXml, "/feed/link/@href", checkpoint, ATOM_DEFINITION);
    }



}
