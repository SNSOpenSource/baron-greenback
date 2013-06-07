package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import java.util.Date;

import static com.googlecode.barongreenback.crawler.failures.Failure.failure;
import static com.googlecode.totallylazy.Exceptions.asString;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FailureHandler {
    private final Failures failures;
    private final Clock clock;

    public FailureHandler(Failures failures, Clock clock) {
        this.failures = failures;
        this.clock = clock;
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
        Date start = job.createdDate();
        try {
            return checkResponseCode(job, function.call(request), elapsedTime(start));
        } catch (Exception e) {
            addFailure(failures, job, asString(e), elapsedTime(start));
            throw e;
        }
    }

    private long elapsedTime(Date start) {
        return new Double(calculateMilliseconds(toNanos(start), toNanos(clock.now()))).longValue();
    }

    private long toNanos(Date start) {
        return MILLISECONDS.toNanos(start.getTime());
    }

    private Response checkResponseCode(StagedJob job, Response response, Long duration) {
        if (!response.status().equals(Status.OK)) {
            addFailure(failures, job, response.toString(), duration);
            return response(Status.NO_CONTENT).build();
        }
        return response;
    }

    private static void addFailure(Failures failures, StagedJob job, String reason, Long duration) {
        failures.add(failure(job, reason, duration));
    }
}