package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.IterableMatcher;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

import static com.googlecode.barongreenback.crawler.CrawlerTest.ATOM_DEFINITION;
import static com.googlecode.barongreenback.crawler.CrawlerTest.FIRST_NAME;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DocumentFeederTest extends CrawlerTests {
    public static final String PILOT_TEST = "http://pilottest-netstream.is.uk.easynet.net:10012/orca/rolo/orderEvents";
    public static final String PRODUCTION = "http://diamond-quartz.is.uk.easynet.net:10011/orca/rolo/orderEvents";
    private HttpHandler client = new AuditHandler(new ClientHttpHandler(), new PrintAuditor(System.out));

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
    public void supportsSubFeeds() throws Exception {
        Feeder<Uri> feeder = new SubFeeder(new UriFeeder(client, "/feed/link/@href"));
        Sequence<Record> records = feeder.get(atomXml, ATOM_DEFINITION);
        assertThat(records.map(FIRST_NAME).filter(Predicates.notNullValue()).realise(), IterableMatcher.hasExactly("Dan", "Matt"));
    }

    @Test
    public void filtersDuplicates() throws Exception {
        Feeder<Uri> feeder = new DuplicateRemover(new UriFeeder(client, ""));
        Sequence<Record> records = feeder.get(atomXml, ATOM_DEFINITION);
        assertThat(records.size(), is(2));
    }

    @Test
    @Ignore
    public void demo() throws Exception {
        Feeder<Uri> feeder = new SubFeeder(new DuplicateRemover(new UriFeeder(client, "")));
        Sequence<Record> records = feeder.get(uri(PILOT_TEST), productionCrawler());
        for (Record record : records) {
            System.out.println("record = " + record);
        }
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

}
