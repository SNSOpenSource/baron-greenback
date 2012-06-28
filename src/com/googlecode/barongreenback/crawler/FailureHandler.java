package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    private final CrawlerFailures crawlerFailures;

    public FailureHandler(CrawlerFailures crawlerFailures) {
        this.crawlerFailures = crawlerFailures;
    }

    public Function1<Response, Response> captureFailures(final StagedJob<Response> job) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                return captureFailures(job, response);
            }
        };
    }

    public Response captureFailures(StagedJob<Response> job, Response response) {
        return captureFailures(job, crawlerFailures, response);
    }

    public static Response captureFailures(StagedJob<Response> job, CrawlerFailures crawlerFailures, Response response) {
        if(!response.status().equals(Status.OK)) {
            crawlerFailures.add(Pair.pair(job, response));
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }
}