package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

import static com.googlecode.yadic.Containers.addActivatorIfAbsent;
import static com.googlecode.yadic.Containers.addIfAbsent;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule {
    public Container addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(BaronGreenbackApplicationScope.class).value();
        addActivatorIfAbsent(container, PartitionedIndex.class, PartitionedIndexActivator.class);
        return applicationScope;
    }

    public Container addPerRequestObjects(final Container requestScope) {
        final Container container = requestScope.get(BaronGreenbackRequestScope.class).value();
        addIfAbsent(container, LuceneMappings.class);
        addActivatorIfAbsent(container, Persistence.class, PersistenceActivator.class);
        addIfAbsent(container, Records.class, LucenePartitionedRecords.class);
        return requestScope;
    }

    public static class PersistenceActivator implements Callable<Persistence> {
        private final PartitionedIndex partitionedIndex;

        public PersistenceActivator(PartitionedIndex partitionedIndex) {
            this.partitionedIndex = partitionedIndex;
        }

        @Override
        public Persistence call() throws Exception {
            return partitionedIndex;
        }
    }
}