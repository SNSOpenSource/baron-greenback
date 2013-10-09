package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failures.FailureHandler;
import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class HttpReader {
    public static Function<Response> getInput(final Job job, Container crawlerScope) {
        Uri uri = job.dataSource().uri();
        HttpClient httpClient = crawlerScope.get(CrawlerHttpClient.class);
        FailureHandler failureHandler = crawlerScope.get(FailureHandler.class);
        return failureHandler.captureFailures(asFunction(httpClient), job).deferApply(get(uri).build()).then(recordVisit(job));
    }

    private static Function1<Response, Response> recordVisit(final Job job) {
        return new Function1<Response, Response>() {
            @Override
            public Response call(Response response) throws Exception {
                job.visited().add(job.dataSource());
                return response;
            }
        };
    }
}