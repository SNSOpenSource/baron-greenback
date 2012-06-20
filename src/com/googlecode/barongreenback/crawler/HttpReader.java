package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Function;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class HttpReader {
    public Function<Response> getInput(Container container, HttpDataSource dataSource) {
        return asFunction(container.get(HttpClient.class)).deferApply(
                get(dataSource.uri()).build()).then(
                container.get(FailureHandler.class).captureFailures(dataSource));
    }
}
