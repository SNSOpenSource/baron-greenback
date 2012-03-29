package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecordsActivator;
import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.lazyrecords.Schemaless;
import com.googlecode.lazyrecords.lucene.LuceneRecords;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.OptimisedStorage;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;
import org.apache.lucene.store.Directory;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule {
    public Module addPerApplicationObjects(Container container) {
        container.add(LuceneIndexConfiguration.class);
        container.addActivator(Directory.class, DirectoryActivator.class);
        container.add(LuceneStorage.class, OptimisedStorage.class);
        container.add(Persistence.class, LucenePersistence.class);
        return this;
    }

    public Module addPerRequestObjects(final Container container) {
        container.addActivator(BaronGreenbackRecords.class, new BaronGreenbackRecordsActivator(container, LuceneRecords.class, Schemaless.class));
        container.add(LuceneMappings.class);
        return this;
    }

}