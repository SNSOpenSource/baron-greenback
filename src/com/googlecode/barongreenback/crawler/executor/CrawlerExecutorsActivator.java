package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.utterlyidle.Application;

import java.util.concurrent.Callable;

public class CrawlerExecutorsActivator implements Callable<CrawlerExecutors> {
    private final BaronGreenbackProperties properties;
    private final Application application;
    private final ExecutorFactory executorFactory;

    public CrawlerExecutorsActivator(BaronGreenbackProperties properties, Application application, ExecutorFactory executorFactory) {
        this.properties = properties;
        this.application = application;
        this.executorFactory = executorFactory;
    }

    @Override
    public CrawlerExecutors call() {
        Integer inputHandlerThreads = getProperty("input.handler.threads", "10");
        Integer inputHandlerCapacity = getProperty("input.handler.capacity", "1000");
        Integer processHandlerThreads = getProperty("process.handler.threads", "10");
        Integer processHandlerCapacity = getProperty("process.handler.capacity", "1000");
        Integer outputHandlerThreads = getProperty("output.handler.threads", "10");
        Integer outputHandlerCapacity = getProperty("output.handler.capacity", "1000");

        return new CrawlerExecutors(inputHandlerThreads, inputHandlerCapacity, processHandlerThreads, processHandlerCapacity,
                outputHandlerThreads, outputHandlerCapacity, application, executorFactory);
    }

    private Integer getProperty(String propertyName, String value) {
        return Integer.valueOf(properties.getProperty(propertyName, value));
    }
}
