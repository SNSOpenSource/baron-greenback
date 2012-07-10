package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class CrawlerActivator implements Callable<Crawler> {
    static final String PROPERTY_NAME = "crawler.class.name";
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
            return container.create(crawlerClass());
        } catch (Exception e) {
            return container.create(DEFAULT);
        }
    }

    public Class crawlerClass() throws ClassNotFoundException {
        return Class.forName(properties.getProperty(PROPERTY_NAME, DEFAULT.getName()));
    }
}
