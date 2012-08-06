package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Status;
import com.googlecode.yadic.Container;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.failures.Failure.failure;
import static com.googlecode.totallylazy.Exceptions.asString;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FailureHandlerTest {
    private final UUID crawlerId = UUID.randomUUID();
    private HttpJob job;
    private final Container scope = testScope();

    private Container testScope() {
        Container scope = FailureTest.testScope();
        scope.add(FailureHandler.class);
        return scope;
    }

    @Before
    public void addCrawlerToRepo() {
        CrawlerRepository crawlerRepository = scope.get(CrawlerRepository.class);
        crawlerRepository.importCrawler(some(crawlerId), CrawlerTests.contentOf("crawlerForFailures.json"));
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        Definition source = AbstractCrawler.sourceDefinition(crawler);
        Definition destination = AbstractCrawler.destinationDefinition(crawler);
        HttpDatasource datasource = HttpDatasource.datasource(uri("/any/uri"), source, Record.constructors.record());
        job = HttpJob.httpJob(crawlerId, datasource, destination, new HashSet<HttpDatasource>());
    }

    @Test
    public void shouldPlaceOnRetryQueueIfResponseIsNotOK() throws Exception {
        Response originalResponse = ResponseBuilder.response(Status.INTERNAL_SERVER_ERROR).build();
        Response response = scope.get(FailureHandler.class).captureFailures(returning(originalResponse), job).call(null);

        assertThat(response.entity().toString(), is(""));
        assertThat(response.status(), is(Status.NO_CONTENT));
        assertThat(scope.get(Failures.class).values().map(Callables.<Failure>second()).contains(failure(job, originalResponse.toString())), is(true));
    }

    @Test
    public void shouldReturnOriginalResponseWhenOk() throws Exception {
        Response originalResponse = ResponseBuilder.response(Status.OK).build();
        Response response = scope.get(FailureHandler.class).captureFailures(returning(originalResponse), job).call(null);

        assertThat(response, is(originalResponse));
        assertThat(scope.get(Failures.class).values().map(Callables.<Failure>second()).size(), is(0));
    }

    private Function1<Request, Response> returning(final Response response) {
        return new Function1<Request, Response>() {
            @Override
            public Response call(Request request) throws Exception {
                return response;
            }
        };
    }

    @Test
    public void shouldPlaceOnRetryQueueIfExceptionThrown() throws Exception {
        final Exception expectedException = new Exception("Failed Request");
        try {
            scope.get(FailureHandler.class).captureFailures(new Function1<Request, Response>() {
                @Override
                public Response call(Request request) throws Exception {
                    throw expectedException;
                }
            }, job).call(null);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            assertThat(e, is(expectedException));
            assertThat(scope.get(Failures.class).values().map(Callables.<Failure>second()).contains(failure(job, asString(e))), is(true));
        }
    }
}
