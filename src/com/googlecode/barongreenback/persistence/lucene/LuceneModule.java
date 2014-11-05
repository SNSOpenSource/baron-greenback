package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.index.IndexCheckerResource;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.CaseInsensitive;
import com.googlecode.lazyrecords.lucene.LucenePartitionedRecords;
import com.googlecode.lazyrecords.lucene.LuceneQueryPreprocessor;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;
import com.googlecode.lazyrecords.lucene.Persistence;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import java.net.URI;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.File;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.URLs.uri;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.yadic.Containers.addActivatorIfAbsent;
import static com.googlecode.yadic.Containers.addIfAbsent;
import static com.googlecode.yadic.Containers.addInstanceIfAbsent;
import static java.lang.String.format;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule, ResourcesModule {
    public static URI fileUrl(String persistenceUri) {
        return uri(format("%s:%s", File.value(), persistenceUri.substring(persistenceUri.indexOf("///"))));
    }

    public static String lucene(DirectoryType type) {
        return format("%s:%s", PersistenceModule.LUCENE, type.value());
    }

    public Container addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(BaronGreenbackApplicationScope.class).value();
        addInstanceIfAbsent(container, NameBasedIndexFacetingPolicy.class, new NameBasedIndexFacetingPolicy(alwaysFalse(String.class)));
        addIfAbsent(container, LuceneStorageActivator.class, NameBasedFacetedLuceneStorageActivator.class);
        addActivatorIfAbsent(container, PartitionedIndex.class, PartitionedIndexActivator.class);
        return applicationScope;
    }

    public Container addPerRequestObjects(final Container requestScope) {
        final Container container = requestScope.get(BaronGreenbackRequestScope.class).value();
        addIfAbsent(container, LuceneMappings.class);
        addActivatorIfAbsent(container, Persistence.class, PersistenceActivator.class);
        addInstanceIfAbsent(requestScope, LuceneQueryPreprocessor.class, CaseInsensitive.luceneQueryPreprocessor());
        addIfAbsent(container, Records.class, LucenePartitionedRecords.class);
        return requestScope;
    }

    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(IndexCheckerResource.class));
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