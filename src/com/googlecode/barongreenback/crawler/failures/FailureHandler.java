package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.totallylazy.Function1;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import static com.googlecode.barongreenback.crawler.failures.Failure.failure;
import static com.googlecode.totallylazy.Exceptions.asString;
import static com.googlecode.utterlyidle.ResponseBuilder.response;

public class FailureHandler {
    private final Failures failures;

    public FailureHandler(Failures failures) {
        this.failures = failures;
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
            addFailure(failures, job, asString(e));
            throw e;
        }
    }

    private Response checkResponseCode(StagedJob job, Response response) {
        if (!response.status().equals(Status.OK)) {
            addFailure(failures, job, response.toString());
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }

    private static void addFailure(Failures failures, StagedJob job, String reason) {
        failures.add(failure(job, reason));
    }
}