package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import java.util.concurrent.BlockingQueue;

import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    public static Function1<Response, Response> captureFailures(final Request request, final BlockingQueue<Pair<Request, Response>> retryQueue) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(response, retryQueue, request);
            }
        };
    }

    private static Response captureFailures(Response response, BlockingQueue<Pair<Request, Response>> retryQueue, Request request) {
        if(!response.status().equals(Status.OK)) {
            retryQueue.add(Pair.pair(request, response));
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }
}
