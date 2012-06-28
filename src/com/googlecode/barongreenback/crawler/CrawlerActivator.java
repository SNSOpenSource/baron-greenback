package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class CrawlerActivator implements Callable<Crawler> {
    private static final String PROPERTY_NAME = "crawler.class.name";
//    private static final Class<?> DEFAULT = QueuesCrawler.class;
    private static final Class<?> DEFAULT = SequentialCrawler.class;
    private final BaronGreenbackProperties properties;
    private final Container container;

    public CrawlerActivator(BaronGreenbackProperties properties, Container container) {
        this.properties = properties;
        this.container = container;
    }

    @Override
    public Crawler call() throws Exception {
        try {
            String className = properties.getProperty(PROPERTY_NAME, DEFAULT.getName());
            return container.create(Class.forName(className));
        } catch (Exception e) {
            return container.create(DEFAULT);
        }
    }
}
