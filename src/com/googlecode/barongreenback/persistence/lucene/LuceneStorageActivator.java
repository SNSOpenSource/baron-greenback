package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.OptimisedStorage;
import com.googlecode.lazyrecords.lucene.mappings.BackgroundStorage;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class LuceneStorageActivator implements Callable<LuceneStorage> {
    private final Boolean backgroundThread;
    private final Container container;

    public LuceneStorageActivator(BaronGreenbackProperties properties, Container container) {
        this.container = container;
        backgroundThread = Boolean.valueOf(properties.getProperty("lucene.background.thread"));
    }

    @Override
    public LuceneStorage call() throws Exception {
        LuceneStorage storage = container.create(OptimisedStorage.class);
        return backgroundThread ? new BackgroundStorage(storage) : storage;
    }
}
