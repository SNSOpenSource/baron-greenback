package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Exceptions;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.ResponseBuilder;
import com.googlecode.utterlyidle.Status;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Test;

import static com.googlecode.totallylazy.Exceptions.*;
import static com.googlecode.totallylazy.Uri.uri;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FailureHandlerTest {
    private static final HttpDatasource DATASOURCE = new HttpDatasource(uri("/any/uri"), null);
    private static final HttpJob JOB = HttpJob.job(DATASOURCE, Definition.constructors.definition(null, null));
    private CrawlerFailures crawlerFailures = new CrawlerFailures();
    private FailureHandler failureHandler = new FailureHandler(crawlerFailures);

    @Test
    public void shouldPlaceOnRetryQueueIfResponseIsNotOK() throws Exception {
        Response originalResponse = ResponseBuilder.response(Status.INTERNAL_SERVER_ERROR).build();
        Response response = failureHandler.captureFailures(returning(originalResponse), JOB).call(null);

        assertThat(response.entity().toString(), is(""));
        assertThat(response.status(), is(Status.NO_CONTENT));
        assertThat(crawlerFailures.values().values().contains(Pair.<StagedJob, String>pair(JOB, originalResponse.toString())), is(true));
    }

    @Test
    public void shouldReturnOriginalResponseWhenOk() throws Exception {
        Response originalResponse = ResponseBuilder.response(Status.OK).build();
        Response response = failureHandler.captureFailures(returning(originalResponse), JOB).call(null);

        assertThat(response, is(originalResponse));
        assertThat(crawlerFailures.values().values().size(), is(0));
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
            failureHandler.captureFailures(new Function1<Request, Response>() {
                @Override
                public Response call(Request request) throws Exception {
                    throw expectedException;
                }
            }, JOB).call(null);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            assertThat(e, is(expectedException));
            assertThat(crawlerFailures.values().values().contains(Pair.<StagedJob, String>pair(JOB, asString(e))), is(true));
        }
    }
}
