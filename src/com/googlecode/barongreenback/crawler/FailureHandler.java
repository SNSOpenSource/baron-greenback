package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import java.util.concurrent.BlockingQueue;

import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    private final BlockingQueue<Pair<HttpDataSource, Response>> retryQueue;

    public FailureHandler(BlockingQueue<Pair<HttpDataSource, Response>> retryQueue) {
        this.retryQueue = retryQueue;
    }

    public Function1<Response, Response> captureFailures(final HttpDataSource dataSource) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(dataSource, response);
            }
        };
    }

    public Response captureFailures(HttpDataSource request, Response response) {
        return captureFailures(request, retryQueue, response);
    }


    public static Response captureFailures(HttpDataSource originalRequest, BlockingQueue<Pair<HttpDataSource, Response>> retryQueue, Response response) {
        if(!response.status().equals(Status.OK)) {
            retryQueue.add(Pair.pair(originalRequest, response));
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }

    public static Function1<Response, Response> captureFailures(final HttpDataSource originalRequest, final BlockingQueue<Pair<HttpDataSource, Response>> retryQueue) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(originalRequest, retryQueue, response);
            }
        };
    }
}
