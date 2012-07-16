package com.googlecode.barongreenback.crawler;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;

public class CrawlerHttpHandler implements HttpHandler {
    private final HttpHandler httpHandler;

    public CrawlerHttpHandler(HttpClient httpClient) {
        httpHandler = httpClient;
    }

    @Override
    public Response handle(Request request) throws Exception {
        return httpHandler.handle(request);
    }
}
