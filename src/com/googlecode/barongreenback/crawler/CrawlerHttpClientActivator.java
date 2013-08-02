package com.googlecode.barongreenback.crawler;

import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.util.concurrent.Callable;

public class CrawlerHttpClientActivator implements Callable<CrawlerHttpClient> {
    private final CrawlerConnectTimeout connectTimeout;
    private final CrawlerReadTimeout readTimeout;

    public CrawlerHttpClientActivator(CrawlerConnectTimeout connectTimeout, CrawlerReadTimeout readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public CrawlerHttpClient call() throws Exception {
        return new CrawlerHttpClient(connectTimeout, readTimeout);
    }
}
