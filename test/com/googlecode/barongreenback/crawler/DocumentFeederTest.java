package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import com.googlecode.utterlyidle.httpserver.RestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static com.googlecode.barongreenback.crawler.CrawlerResourceTest.setupServerWithDataFeed;
import static com.googlecode.barongreenback.crawler.CrawlerTest.ATOM_DEFINITION;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class DocumentFeederTest {
    private HttpHandler client = new AuditHandler(new ClientHttpHandler(), new PrintAuditor(System.out));
    private RestServer restServer;
    private Uri atomXml = Uri.uri("http://localhost:9001/data");

    @Test
    public void supportsGettingRecords() throws Exception {
        Feeder<Uri> feeder = new UriFeeder(client, "");
        Sequence<Record> records = feeder.get(atomXml, ATOM_DEFINITION);
        assertThat(records.size(), is(2));
    }

    @Test
    public void supportsStoppingWhenCheckpointIsReached() throws Exception {
        Feeder<Uri> feeder = new CheckPointStopper(date("2011-07-19T12:43:25Z"), new UriFeeder(client, ""));
        Sequence<Record> records = feeder.get(atomXml, ATOM_DEFINITION);
        assertThat(records.size(), is(1));
    }

    @Test
    public void supportsFollowingMore() throws Exception {
        Feeder<Uri> feeder = new UriFeeder(client, "/feed/link/@href");
        Sequence<Record> records = feeder.get(atomXml, ATOM_DEFINITION);
        assertThat(records.size(), is(4));
    }

    @Test
    @Ignore
    public void demo() throws Exception {
        Feeder<Uri> feeder = new CheckPointStopper(date("2011-11-22T13:29:57Z"), new UriFeeder(client, ""));
        Sequence<Record> records = feeder.get(uri("http://pilottest-netstream.is.uk.easynet.net:10012/orca/rolo/orderEvents"), productionCrawler());
        assertThat(records.size(), is(1));
    }

    private RecordDefinition productionCrawler() {
        Model model = Model.parse(Strings.toString(new File("/home/dev/trunk/netstream/penfold/penfold-core/src/main/java/sky/sns/penfold/rest/barongreenback/orderCrawler.json")));
        Model form = model.get("form", Model.class);
        Model record = form.get("record", Model.class);
        return convert(record);
    }

    private Date date(String value) throws ParseException {
        return Dates.RFC3339().parse(value);
    }

    @Before
    public void startWaitrest() throws Exception {
        restServer = setupServerWithDataFeed();
    }

    @After
    public void stopWaitrest() throws IOException {
        restServer.close();
    }

}
