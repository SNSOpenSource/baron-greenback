package com.sky.sns.barongreenback.crawler.failures;

import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;
import com.googlecode.yadic.Container;
import com.sky.sns.barongreenback.persistence.BaronGreenbackRecords;
import com.sky.sns.barongreenback.shared.ApplicationTests;
import com.sky.sns.barongreenback.shared.ModelRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.FAILURES;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.ID;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.JOB_TYPE;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.SOURCE;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.URI;
import static com.sky.sns.barongreenback.shared.ModelRepository.MODELS;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FailureResourceTest extends ApplicationTests {

    private CrawlerFailuresPage page;
    public static final UUID CRAWLER_ID = UUID.randomUUID();

    @Before
    public void setup(){
        page = new CrawlerFailuresPage(browser);
     }

    @Test
    public void shouldReturnAnEmptyListIfNoFailures() throws Exception {
        page.search("");

        assertEquals(0, page.getFailuresCount());
    }

    @Test
    public void shouldReturnAllFailuresIfEmptyQuery() throws Exception {
        givenCrawlerFailuresForUrls("http://test1.sns.com", "http://test1.sns.com", "http://test2.sns.com");
        page.search("");

        assertEquals(3, page.getFailuresCount());
    }

    @Test
    public void shouldReturnAllFailuresWithGivenUrl() throws Exception {
        givenCrawlerFailuresForUrls("http://test1.sns.com", "http://test1.sns.com", "http://test2.sns.com");
        page.search("\"http://test1.sns.com\"");

        assertEquals(2, page.getFailuresCount());
        assertThat(page.getFailureUrls(), contains("http://test1.sns.com", "http://test1.sns.com"));
    }

    @Test
    public void shouldReturnNoResultsIfNoFailuresForWithGivenUrl() throws Exception {
        givenCrawlerFailuresForUrls("http://test1.sns.com", "http://test1.sns.com", "http://test2.sns.com");
        page.search("\"http://test4.sns.com\"");

        assertEquals(0, page.getFailuresCount());
    }

    @Test
    public void shouldDeleteOnlySelectedFailures() throws Exception {
        givenCrawlerFailuresForUrls("http://test1.sns.com", "http://test1.sns.com", "http://test2.sns.com");
        page.deleteAll("\"http://test1.sns.com\"");

        assertEquals(0, page.getFailuresCount());
        assertThat(page.getMessage(), is("2 failures(s) have been deleted"));
    }


    private void givenCrawlerFailuresForUrls(final String... urls) {
        final Sequence<Record> failureRecords = Sequences.sequence(urls).map(new Mapper<String, Record>() {
            @Override
            public Record call(String uri) throws Exception {
                return Record.constructors.record(ID, UUID.randomUUID(), URI, new Uri(uri));
            }
        });
        application.usingRequestScope(new Block<Container>() {
            @Override
            public void execute(Container container) throws Exception {
                Records records = container.get(BaronGreenbackRecords.class).value();
                records.add(FAILURES, failureRecords);
            }
        });
    }
}