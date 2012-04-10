package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceApplicationScope;
import com.googlecode.barongreenback.persistence.PersistenceRequestScope;
import com.googlecode.lazyrecords.Records;
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
    public Module addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(PersistenceApplicationScope.class).value();
        container.addActivator(Directory.class, DirectoryActivator.class);
        container.add(LuceneStorage.class, OptimisedStorage.class);
        return this;
    }

    public Module addPerRequestObjects(final Container requestScope) {
        final Container container = requestScope.get(PersistenceRequestScope.class).value();
        container.add(Persistence.class, LucenePersistence.class);
        container.add(LuceneMappings.class);
        container.add(Records.class, LuceneRecords.class);
        return this;
    }

}