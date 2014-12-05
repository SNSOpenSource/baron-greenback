package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.index.IndexCheckerResource;
import com.googlecode.barongreenback.persistence.PersistenceModule;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.services.Services;
import com.googlecode.utterlyidle.services.ServicesModule;
import com.googlecode.utterlyidle.services.StartOnlyService;
import com.googlecode.yadic.Container;
import org.apache.lucene.search.BooleanQuery;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.lucene.DirectoryType.File;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.URLs.uri;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.yadic.Containers.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class LuceneModule implements ApplicationScopedModule, RequestScopedModule, ResourcesModule, ServicesModule {

    public static final String LUCENE_MAX_CLAUSE_COUNT = "lucene.max.clause.count";

    public static URI fileUrl(String persistenceUri) {
        return uri(format("%s:%s", File.value(), persistenceUri.substring(persistenceUri.indexOf("///"))));
    }

    public static String lucene(DirectoryType type) {
        return format("%s:%s", PersistenceModule.LUCENE, type.value());
    }

    public Container addPerApplicationObjects(Container applicationScope) {
        final Container container = applicationScope.get(BaronGreenbackApplicationScope.class).value();
        addInstanceIfAbsent(container, NameBasedIndexFacetingPolicy.class, new NameBasedIndexFacetingPolicy(alwaysFalse(String.class)));
        addActivatorIfAbsent(container, NameToLuceneDirectoryFunction.class, NameToLuceneDirectoryFunctionActivator.class);
        addActivatorIfAbsent(container, NameToLuceneStorageFunction.class, CaseInsensitiveNameToLuceneStorageFunctionActivator.class);
        container.decorate(NameToLuceneStorageFunction.class, TaxonomyNameToLuceneStorageFunction.class);
        addIfAbsent(container, PartitionedIndex.class, LucenePartitionedIndex.class);
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

    @Override
    public Services add(Services services) throws Exception {
        return services.addAndRegister(MaxClauseCountConfigurer.class);
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

    public static class MaxClauseCountConfigurer extends StartOnlyService {

        private final Properties properties;

        public MaxClauseCountConfigurer(Properties properties) {
            this.properties = properties;
        }

        @Override
        public void start() throws Exception {
            BooleanQuery.setMaxClauseCount(parseInt(properties.getProperty(LUCENE_MAX_CLAUSE_COUNT, "4096")));
        }
    }
}