package com.sky.sns.barongreenback.crawler;

import com.googlecode.utterlyidle.SmartHttpClient;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.InternalHttpHandler;

import java.util.concurrent.Callable;

public class CrawlerHttpClientActivator implements Callable<CrawlerHttpClient> {
    private final CrawlerConnectTimeout connectTimeout;
    private final CrawlerReadTimeout readTimeout;
    private final InternalHttpHandler internalHttpHandler;

    public CrawlerHttpClientActivator(CrawlerConnectTimeout connectTimeout, CrawlerReadTimeout readTimeout, InternalHttpHandler internalHttpHandler) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.internalHttpHandler = internalHttpHandler;
    }

    @Override
    public CrawlerHttpClient call() throws Exception {
        return new CrawlerHttpClient(new SmartHttpClient(internalHttpHandler, new ClientHttpHandler(connectTimeout.value(), readTimeout.value())));
    }
}
