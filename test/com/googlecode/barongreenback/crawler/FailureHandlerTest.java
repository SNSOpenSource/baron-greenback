package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.*;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingDeque;

import static com.googlecode.totallylazy.Pair.pair;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FailureHandlerTest {
    @Test
    public void shouldPlaceOnRetryQueueIfFailed() throws Exception {
        LinkedBlockingDeque<Pair<Request, Response>> retryQueue = new LinkedBlockingDeque<Pair<Request, Response>>();
        Request request = RequestBuilder.get("/any/uri").build();
        Function1<Response, Response> failer = FailureHandler.captureFailures(request, retryQueue);
        Response originalResponse = ResponseBuilder.response(Status.NOT_FOUND).build();
        Response response = failer.apply(originalResponse);
        assertThat(response.entity().toString(), is(""));
        assertThat(response.status(), is(Status.NO_CONTENT));
        assertThat(retryQueue.contains(pair(request, originalResponse)), is(true));
    }

    @Test
    public void shouldReturnOriginalResponseWhenOk() throws Exception {
        LinkedBlockingDeque<Pair<Request, Response>> retryQueue = new LinkedBlockingDeque<Pair<Request, Response>>();
        Function1<Response, Response> failer = FailureHandler.captureFailures(RequestBuilder.get("/any/uri").build(), retryQueue);
        Response expectedResponse = ResponseBuilder.response(Status.OK).build();
        Response response = failer.apply(expectedResponse);
        assertThat(response, is(expectedResponse));
        assertThat(retryQueue.size(), is(0));
    }
}
