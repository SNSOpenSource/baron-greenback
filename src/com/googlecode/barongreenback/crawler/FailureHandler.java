package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import java.util.concurrent.BlockingQueue;

import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    private final BlockingQueue<Pair<Request, Response>> retryQueue;

    public FailureHandler(BlockingQueue<Pair<Request, Response>> retryQueue) {
        this.retryQueue = retryQueue;
    }

    public Function1<Response, Response> captureFailures(final Request request) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(request, response);
            }
        };
    }

    public Response captureFailures(Request request, Response response) {
        return captureFailures(request, retryQueue, response);
    }


    public static Response captureFailures(Request originalRequest, BlockingQueue<Pair<Request, Response>> retryQueue, Response response) {
        if(!response.status().equals(Status.OK)) {
            retryQueue.add(Pair.pair(originalRequest, response));
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }

    public static Function1<Response, Response> captureFailures(final Request originalRequest, final BlockingQueue<Pair<Request, Response>> retryQueue) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(originalRequest, retryQueue, response);
            }
        };
    }
}
