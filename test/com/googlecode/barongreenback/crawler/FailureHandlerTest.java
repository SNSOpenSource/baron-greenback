package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.*;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.FailureHandler.captureFailures;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Uri.uri;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FailureHandlerTest {
    @Test
    public void shouldPlaceOnRetryQueueIfFailed() throws Exception {
        CrawlerFailures crawlerFailures = new CrawlerFailures();
        FailureHandler failureHandler = new FailureHandler(crawlerFailures);
        HttpDatasource datasource = new HttpDatasource(uri("/any/uri"), null);
        Response originalResponse = ResponseBuilder.response(Status.NOT_FOUND).build();
        HttpJob expectedJob = HttpJob.job(new SimpleContainer(), datasource, Definition.constructors.definition(null, null));
        Response response = failureHandler.captureFailures(expectedJob, originalResponse);
        assertThat(response.entity().toString(), is(""));
        assertThat(response.status(), is(Status.NO_CONTENT));
        assertThat(crawlerFailures.values().values().contains(Pair.<StagedJob<Response>, Response>pair(expectedJob, originalResponse)), is(true));
    }

    @Test
    public void shouldReturnOriginalResponseWhenOk() throws Exception {
        CrawlerFailures crawlerFailures = new CrawlerFailures();
        HttpDatasource datasource = new HttpDatasource(uri("/any/uri"), null);
        Response expectedResponse = ResponseBuilder.response(Status.OK).build();
        Response response = captureFailures(HttpJob.job(new SimpleContainer(), datasource, Definition.constructors.definition(null, null)), crawlerFailures, expectedResponse);
        assertThat(response, is(expectedResponse));
        assertThat(crawlerFailures.values().values().size(), is(0));
    }
}
