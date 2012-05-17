package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.lazyrecords.lucene.OptimisedPool;
import com.googlecode.lazyrecords.lucene.SearcherPool;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class SearcherPoolActivator implements Callable<SearcherPool> {
    private static final String PROPERTY_NAME = "searcher.pool.class.name";
    private final BaronGreenbackProperties properties;
    private final Container container;

    public SearcherPoolActivator(BaronGreenbackProperties properties, Container container) {
        this.properties = properties;
        this.container = container;
    }

    @Override
    public SearcherPool call() throws Exception {
        try {
            String className = properties.getProperty(PROPERTY_NAME, OptimisedPool.class.getName());
            return container.create(Class.forName(className));
        } catch (Exception e) {
            return container.create(OptimisedPool.class);
        }
    }
}
