package com.googlecode.barongreenback.crawler.executor;

import java.util.Properties;
import java.util.concurrent.Callable;

public class CrawlerExecutorsActivator implements Callable<CrawlerExecutors> {

    private final Properties properties;

    public CrawlerExecutorsActivator(Properties properties) {
        this.properties = properties;
    }

    @Override
    public CrawlerExecutors call() {
        try {
            Integer inputHandlerThreads = setProperty("input.handler.threads", "10");
            Integer inputHandlerCapacity = setProperty("input.handler.capacity", "0");
            Integer processHandlerThreads = setProperty("process.handler.threads", "1");
            Integer processHandlerCapacity = setProperty("process.handler.capacity", "50");
            Integer outputHandlerThreads = setProperty("output.handler.threads", "1");
            Integer outputHandlerCapacity = setProperty("output.handler.capacity", "0");

            return new CrawlerExecutors(inputHandlerThreads,inputHandlerCapacity,processHandlerThreads,processHandlerCapacity,
                                        outputHandlerThreads,outputHandlerCapacity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private Integer setProperty(String propertyName, String value) {
        return Integer.valueOf(properties.getProperty(propertyName, value));
    }
}
