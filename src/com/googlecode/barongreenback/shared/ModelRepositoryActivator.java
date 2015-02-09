package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;

import java.util.Properties;
import java.util.concurrent.Callable;

import static java.lang.Boolean.parseBoolean;

public class ModelRepositoryActivator implements Callable<ModelRepository> {

    public static final String CACHE_ENABLED_DEFAULT = "true";
    public static final String CACHE_ENABLED_PROPERTY_NAME = "barongreenback.modelrepository.cache.enabled";

    private final boolean cached;
    private final BaronGreenbackRecords records;
    private final ModelCache modelCache;

    public ModelRepositoryActivator(Properties properties, BaronGreenbackRecords records, ModelCache modelCache) {
        this.records = records;
        this.modelCache = modelCache;
        this.cached = parseBoolean(properties.getProperty(CACHE_ENABLED_PROPERTY_NAME, CACHE_ENABLED_DEFAULT));
    }

    @Override
    public ModelRepository call() throws Exception {
        final RecordsModelRepository uncachedRepository = new RecordsModelRepository(records);
        return cached
                ? new CachingModelRepository(uncachedRepository, modelCache)
                : uncachedRepository;
    }
}
