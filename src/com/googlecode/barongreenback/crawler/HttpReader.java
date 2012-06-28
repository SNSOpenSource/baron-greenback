package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class HttpReader {
    public static Function<Response> getInput(StagedJob<Response> job) {
        Uri uri = job.dataSource().uri();
        return asFunction(job.container().get(HttpClient.class)).deferApply(
                get(uri).build()).then(
                job.container().get(FailureHandler.class).captureFailures(job));
    }
}