package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.utterlyidle.Server;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CrawlerTest {
    private static final Keyword<Object> USER = keyword("/user");
    private static final Keyword<Integer> USER_ID = keyword("summary/userId", Integer.class).metadata(record().set(Keywords.UNIQUE, true));
    private static final Keyword<String> FIRST_NAME = keyword("summary/firstName", String.class);


    private static final Keyword<Object> ENTRIES = keyword("/feed/entry");
    private static final Keyword<String> ID = keyword("id", String.class).metadata(record().set(Keywords.UNIQUE, false).set(Views.VISIBLE, true));
    private static final Keyword<URI> LINK = keyword("link/@href", URI.class).
            metadata(MapRecord.record().set(RecordDefinition.RECORD_DEFINITION, new RecordDefinition(USER, Sequences.<Keyword>sequence(USER_ID, FIRST_NAME))));
    private static final Keyword<Date> UPDATED = keyword("updated", Date.class);
    private static final Keyword<String> TITLE = keyword("title", String.class);


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

    public static Sequence<Record> crawl(Uri feed) throws Exception {
        return new Crawler().crawl(feed.toURL(), defintion());
    }

    private static RecordDefinition defintion() {
        return new RecordDefinition(ENTRIES, Sequences.<Keyword>sequence(ID, LINK, UPDATED, TITLE));
    }

    public static Uri createFeed(final Server server) {
        return server.uri().path("static/atom.xml");
    }


    public static Server startServer() {
        return application().content(packageUrl(CrawlerTest.class), "static").start(defaultConfiguration().port(10010));
    }

}
