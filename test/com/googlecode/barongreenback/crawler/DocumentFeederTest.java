package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.IterableMatcher;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.ATOM_DEFINITION;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.ENTRY_DEFINITION;
import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.FIRST;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DocumentFeederTest extends CrawlerTests {
    private HttpHandler client() {
        return  new AuditHandler(feedClient(), new PrintAuditor(System.out));
    }

    @Test
    public void supportsAliases() throws Exception{
        Document document = document(contentOf("entry1.xml"));
        Feeder<Document> feeder = new DocumentFeeder();
        Sequence<Record> records = feeder.get(document, ENTRY_DEFINITION);
        Record record = records.head();
        assertThat(record.get(FIRST), Matchers.is("Dan"));
    }

    @Test
    public void ignoresInvalidXml() throws Exception {
        Feeder<Uri> feeder = new UriFeeder(client(), "");
        Sequence<Record> records = feeder.get(uri("http://localhost:9001/invalid.xml"), null);
        assertThat(records.isEmpty(), CoreMatchers.is(true));
    }

    @Test
    public void supportsGettingRecords() throws Exception {
        Feeder<Uri> feeder = new UriFeeder(client(), "");
        Sequence<Record> records = feeder.get(feed(), ATOM_DEFINITION);
        assertThat(records.size(), is(2));
    }

    @Test
    public void supportsStoppingWhenCheckpointIsReached() throws Exception {
        Feeder<Uri> feeder = new CheckPointStopper("2011-07-19T12:43:25.000Z", new UriFeeder(client(), ""));
        Sequence<Record> records = feeder.get(feed(), ATOM_DEFINITION);
        assertThat(records.size(), is(1));
    }

    @Test
    public void supportsFollowingMore() throws Exception {
        Feeder<Uri> feeder = new UriFeeder(client(), "/feed/link/@href");
        Sequence<Record> records = feeder.get(feed(), ATOM_DEFINITION);
        assertThat(records.size(), is(4));
    }

    @Test
    public void supportsSubFeeds() throws Exception {
        Feeder<Uri> feeder = new SubFeeder(new UriFeeder(client(), "/feed/link/@href"));
        Sequence<Record> records = feeder.get(feed(), ATOM_DEFINITION);
        assertThat(records.map(FIRST).filter(Predicates.notNullValue()).realise(), IterableMatcher.hasExactly("Dan", "Matt"));
    }

    @Test
    public void filtersDuplicates() throws Exception {
        Feeder<Uri> feeder = new DuplicateRemover(new UriFeeder(client(), ""));
        Sequence<Record> records = feeder.get(feed(), ATOM_DEFINITION);
        assertThat(records.size(), is(2));
    }
}
