package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Unary;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;

import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.Functions.identity;

public class CrawlerHttpClient implements HttpClient {
    private final HttpHandler httpHandler;

    public CrawlerHttpClient(Unary<HttpHandler> handlerGenerator, CrawlerConnectTimeout connectTimeout, CrawlerReadTimeout readTimeout) {
        httpHandler = Callers.call(handlerGenerator, new ClientHttpHandler(connectTimeout.value(), readTimeout.value()));
    }

    @Override
    public Response handle(Request request) throws Exception {
        return httpHandler.handle(request);
    }
}
