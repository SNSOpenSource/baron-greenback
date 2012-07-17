package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import static com.googlecode.totallylazy.Exceptions.asString;
import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    private final CrawlerFailures crawlerFailures;

    public FailureHandler(CrawlerFailures crawlerFailures) {
        this.crawlerFailures = crawlerFailures;
    }

    public Function1<Request, Response> captureFailures(final Function1<Request, Response> function, final StagedJob job) {
        return new Function1<Request, Response>() {
            @Override
            public Response call(Request request) throws Exception {
                return captureFailures(function, request, job);
            }
        };
    }

    private Response captureFailures(Function1<Request, Response> function, Request request, StagedJob job) throws Exception {
        try {
            return checkResponseCode(job, function.call(request));
        } catch (Exception e) {
            addFailure(crawlerFailures, job, asString(e));
            throw e;
        }
    }

    private Response checkResponseCode(StagedJob job, Response response) {
        if (!response.status().equals(Status.OK)) {
            addFailure(crawlerFailures, job, response.toString());
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }

    private static void addFailure(CrawlerFailures crawlerFailures, StagedJob job, String reason) {
        crawlerFailures.add(Pair.pair(job, reason));
    }
}