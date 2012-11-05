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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.yadic.Containers.addActivatorIfAbsent;
import static com.googlecode.yadic.Containers.addIfAbsent;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule {
    public Container addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(PersistenceApplicationScope.class).value();
        addActivatorIfAbsent(container, Directory.class, DirectoryActivator.class);
        addActivatorIfAbsent(container, LuceneStorage.class, LuceneStorageActivator.class);
        addActivatorIfAbsent(container, SearcherPool.class, SearcherPoolActivator.class);
        return applicationScope;
    }

    public Container addPerRequestObjects(final Container requestScope) {
        final Container container = requestScope.get(PersistenceRequestScope.class).value();
        addIfAbsent(container, Persistence.class, LucenePersistence.class);
        addIfAbsent(container, LuceneMappings.class);
        addActivatorIfAbsent(container, Records.class, RecordsActivator.class);
        return requestScope;
    }

    public static class RecordsActivator implements Callable<Records>, Closeable {
        private final Container container;
        private LuceneRecords records;

        public RecordsActivator(Container container) {
            this.container = container;
        }

        @Override
        public Records call() throws Exception {
            return records = container.create(LuceneRecords.class);
        }

        @Override
        public void close() throws IOException {
            records.close();
        }
    }
}