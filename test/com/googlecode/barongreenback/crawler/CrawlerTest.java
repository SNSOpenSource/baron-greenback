package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.totallylazy.time.DateFormatConverter;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.Server;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static com.googlecode.barongreenback.crawler.Crawler.CHECKPOINT_VALUE;
import static com.googlecode.barongreenback.crawler.Crawler.DOCUMENT;
import static com.googlecode.barongreenback.crawler.Crawler.URL;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerTest {
    public static final Keyword<Object> USER = keyword("/user");
    public static final Keyword<Integer> USER_ID = keyword("summary/userId", Integer.class).metadata(record().set(Keywords.UNIQUE, true));
    public static final Keyword<String> FIRST_NAME = keyword("summary/firstName", String.class).as(keyword("first", String.class));

    public static final Keyword<Object> ENTRIES = keyword("/feed/entry");
    public static final Keyword<String> ID = keyword("id", String.class).metadata(record().set(Keywords.UNIQUE, false).set(Views.VISIBLE, true));
    public static final Keyword<URI> LINK = keyword("link/@href", URI.class).
            metadata(MapRecord.record().set(RECORD_DEFINITION, new RecordDefinition(USER, Sequences.<Keyword>sequence(USER_ID, FIRST_NAME))));
    public static final Keyword<Date> UPDATED = keyword("updated", Date.class).metadata(record().set(Crawler.CHECKPOINT, true));
    public static final Keyword<String> TITLE = keyword("title", String.class);

    public static final RecordDefinition ATOM_DEFINITION = new RecordDefinition(ENTRIES, Sequences.<Keyword>sequence(ID, LINK, UPDATED, TITLE));

    @Test
    public void shouldGetTheContentsOfAUrlAndExtractContent() throws Exception {
        Server server = startServer();
        final Uri feed = createFeed(server);
        Sequence<Record> records = crawl(feed);
        Record entry = records.head();

        assertThat(entry.get(ID), is("urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1"));
        assertThat(entry.get(USER_ID), is(1234));
        assertThat(entry.get(FIRST_NAME), is("Dan"));
        server.close();
    }

    @Test
    public void shouldNotGoPastTheCheckpoint_checkpointValueMatchesExactly() throws Exception {
        shouldNotGoPastTheCheckpoint(toDate("2011-07-19T12:43:25Z"));
    }

    @Test
    public void shouldNotGoPastTheCheckpoint_checkpointValueDoesntMatchExactly() throws Exception {
        shouldNotGoPastTheCheckpoint(toDate("2011-07-19T12:43:26Z"));
    }

    private Date toDate(String s) {
        return new DateFormatConverter(Dates.RFC3339()).toDate(s);
    }

    private void shouldNotGoPastTheCheckpoint(Date date) throws Exception {
        Record documentCrawlingDefinition = record().set(DOCUMENT, document(fileContent("atom.xml"))).set(RECORD_DEFINITION, ATOM_DEFINITION).set(CHECKPOINT_VALUE, date);
        Sequence<Record> records = new Crawler().crawlDocument(documentCrawlingDefinition).first();

        assertThat(records.size(), Matchers.<Number>is(1));
    }

    public static Sequence<Record> crawl(Uri feed) throws Exception {
        return new Crawler().crawl(record().set(URL, feed.toURL()).set(RECORD_DEFINITION, ATOM_DEFINITION)).second();
    }

    public static Uri createFeed(final Server server) {
        return server.uri().mergePath("static/atom.xml");
    }

    private String fileContent(String name) {
        return Strings.toString(getClass().getResourceAsStream(name));
    }

    public static Server startServer() {
        return application().content(packageUrl(CrawlerTest.class), "/static").start(defaultConfiguration().port(10010));
    }

}
