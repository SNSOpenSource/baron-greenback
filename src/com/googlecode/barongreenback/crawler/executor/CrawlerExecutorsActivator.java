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
        Integer inputHandlerThreads = setProperty("input.handler.threads", "10");
        Integer inputHandlerCapacity = setProperty("input.handler.capacity", "0");
        Integer processHandlerThreads = setProperty("process.handler.threads", "10");
        Integer processHandlerCapacity = setProperty("process.handler.capacity", "0");
        Integer outputHandlerThreads = setProperty("output.handler.threads", "1");
        Integer outputHandlerCapacity = setProperty("output.handler.seconds", "1");

        return new CrawlerExecutors(inputHandlerThreads, inputHandlerCapacity, processHandlerThreads, processHandlerCapacity,
                outputHandlerThreads, outputHandlerCapacity, application);
    }

    private Integer setProperty(String propertyName, String value) {
        return Integer.valueOf(properties.getProperty(propertyName, value));
    }
}
