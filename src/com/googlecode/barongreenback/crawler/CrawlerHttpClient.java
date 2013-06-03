package com.googlecode.barongreenback.crawler;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;

public class CrawlerHttpClient implements HttpClient {
    private final HttpHandler httpHandler;

    public CrawlerHttpClient(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public CrawlerHttpClient(CrawlerConnectTimeout connectTimeout, CrawlerReadTimeout readTimeout) {
        httpHandler = new ClientHttpHandler(connectTimeout.value(), readTimeout.value());
    }

    @Override
    public Response handle(Request request) throws Exception {
        return httpHandler.handle(request);
    }
}
