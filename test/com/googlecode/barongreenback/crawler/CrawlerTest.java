package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URI;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerTest extends CrawlerTests{
    public static final Keyword<Object> USER = keyword("/user");
    public static final Keyword<Integer> USER_ID = keyword("summary/userId", Integer.class).metadata(record().set(Keywords.UNIQUE, true));
    public static final Keyword<String> FIRST = Keywords.keyword("first", String.class);
    public static final Keyword<String> FIRST_NAME = keyword("summary/firstName", String.class).as(FIRST);

    public static final RecordDefinition ENTRY_DEFINITION = new RecordDefinition(USER, Sequences.<Keyword>sequence(USER_ID, FIRST_NAME));

    public static final Keyword<Object> ENTRIES = keyword("/feed/entry");
    public static final Keyword<String> ID = keyword("id", String.class).metadata(record().set(Keywords.UNIQUE, true).set(Views.VISIBLE, true));
    public static final Keyword<URI> LINK = keyword("link/@href", URI.class).
            metadata(record().set(Keywords.UNIQUE, true).set(RECORD_DEFINITION, ENTRY_DEFINITION));
    public static final Keyword<String> UPDATED = keyword("updated", String.class).metadata(record().set(Crawler.CHECKPOINT, true));
    public static final Keyword<String> TITLE = keyword("title", String.class);

    public static final RecordDefinition ATOM_DEFINITION = new RecordDefinition(ENTRIES, Sequences.<Keyword>sequence(ID, LINK, UPDATED, TITLE));

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
        Sequence<Record> records = crawl("2011-07-19T12:43:25Z");
        assertThat(records.size(), Matchers.<Number>is(1));
    }

    public static Sequence<Record> crawl() throws Exception {
        return crawl(null);
    }

    public static Sequence<Record> crawlOnePageOnly() throws Exception {
        return new Crawler().crawl(atomXml, "", null, ATOM_DEFINITION);
    }

    public static Sequence<Record> crawl(Object checkpoint) throws Exception {
        return new Crawler().crawl(atomXml, "/feed/link/@href", checkpoint, ATOM_DEFINITION);
    }



}
