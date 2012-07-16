package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class HttpReader {
    public static Function<Response> getInput(StagedJob job) {
        Uri uri = job.dataSource().uri();
        HttpClient httpClient = job.container().get(HttpClient.class);
        FailureHandler failureHandler = job.container().get(FailureHandler.class);
        return failureHandler.captureFailures(asFunction(httpClient), job).deferApply(get(uri).build());
    }
}