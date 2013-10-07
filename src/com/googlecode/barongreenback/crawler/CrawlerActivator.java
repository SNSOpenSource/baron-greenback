package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.crawler.CrawlerImplementationResource.ACTIVE_CRAWLER_ID;
import static com.googlecode.funclate.Model.functions.value;

public class CrawlerActivator implements Callable<Crawler> {
    public static final String PROPERTY_NAME = "crawler.class.name";
    private static final Class<?> DEFAULT = QueuesCrawler.class;
    private final BaronGreenbackProperties properties;
    private final Container container;
    private final ModelRepository modelRepository;

    public CrawlerActivator(BaronGreenbackProperties properties, Container container, ModelRepository modelRepository) {
        this.properties = properties;
        this.container = container;
        this.modelRepository = modelRepository;
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
        Option<String> fromRepo = modelRepository.get(ACTIVE_CRAWLER_ID).map(value("crawler", String.class));
        return Class.forName(fromRepo.getOrElse(properties.getProperty(PROPERTY_NAME, DEFAULT.getName())));
    }
}
