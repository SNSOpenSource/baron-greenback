package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.utterlyidle.Application;

import java.util.concurrent.Callable;

public class CrawlerExecutorsActivator implements Callable<CrawlerExecutors> {
    private final BaronGreenbackProperties properties;
    private final Application application;

    public CrawlerExecutorsActivator(BaronGreenbackProperties properties, Application application) {
        this.properties = properties;
        this.application = application;
    }

    @Override
    public CrawlerExecutors call() {
        Integer inputHandlerThreads = getProperty("input.handler.threads", "10");
        Integer inputHandlerCapacity = getProperty("input.handler.capacity", "0");
        Integer processHandlerThreads = getProperty("process.handler.threads", "10");
        Integer processHandlerCapacity = getProperty("process.handler.capacity", "0");
        Integer outputHandlerThreads = getProperty("output.handler.threads", "1");
        Integer outputHandlerCapacity = getProperty("output.handler.capacity", "1000");

        return new CrawlerExecutors(inputHandlerThreads, inputHandlerCapacity, processHandlerThreads, processHandlerCapacity,
                outputHandlerThreads, outputHandlerCapacity, application);
    }

    private Integer getProperty(String propertyName, String value) {
        return Integer.valueOf(properties.getProperty(propertyName, value));
    }
}
