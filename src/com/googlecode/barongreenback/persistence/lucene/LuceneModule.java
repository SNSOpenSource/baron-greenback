package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceApplicationScope;
import com.googlecode.barongreenback.persistence.PersistenceRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;
import org.apache.lucene.store.Directory;

import java.util.concurrent.Callable;

import static com.googlecode.yadic.Containers.addActivatorIfAbsent;
import static com.googlecode.yadic.Containers.addIfAbsent;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule {
    public Module addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(PersistenceApplicationScope.class).value();
        addActivatorIfAbsent(container, Directory.class, DirectoryActivator.class);
        addIfAbsent(container, LuceneStorage.class, OptimisedStorage.class);
        addActivatorIfAbsent(container, SearcherPool.class, SearcherPoolActivator.class);
        return this;
    }

    public Module addPerRequestObjects(final Container requestScope) {
        final Container container = requestScope.get(PersistenceRequestScope.class).value();
        addIfAbsent(container, Persistence.class, LucenePersistence.class);
        addIfAbsent(container, LuceneMappings.class);
        addIfAbsent(container, Records.class, LuceneRecords.class);
        return this;
    }

}