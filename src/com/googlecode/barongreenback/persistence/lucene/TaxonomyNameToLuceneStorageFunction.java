package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.lazyrecords.lucene.FieldBasedFacetingPolicy;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;
import com.googlecode.lazyrecords.lucene.NameToLuceneStorageFunction;
import com.googlecode.lazyrecords.lucene.TaxonomyFacetedLuceneStorage;
import com.googlecode.totallylazy.CloseableList;
import org.apache.lucene.facet.FacetsConfig;

import java.io.IOException;

public class TaxonomyNameToLuceneStorageFunction implements NameToLuceneStorageFunction {
    public static final String TAXONOMY_SUFFIX = "-taxonomy";

    private final NameToLuceneStorageFunction underlyingStorageActivator;
    private final NameToLuceneDirectoryFunction directoryActivator;

    private final NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy;
    private final FieldBasedFacetingPolicy fieldBasedFacetingPolicy;
    private CloseableList closeables = new CloseableList();

    public TaxonomyNameToLuceneStorageFunction(NameToLuceneStorageFunction underlyingStorageActivator, NameToLuceneDirectoryFunction directoryActivator, NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy, FieldBasedFacetingPolicy fieldBasedFacetingPolicy) {
        this.underlyingStorageActivator = underlyingStorageActivator;
        this.directoryActivator = directoryActivator;
        this.nameBasedIndexFacetingPolicy = nameBasedIndexFacetingPolicy;
        this.fieldBasedFacetingPolicy = fieldBasedFacetingPolicy;
    }

    @Override
    public LuceneStorage getForName(String name) {
        final LuceneStorage underlyingStorage = closeables.manage(underlyingStorageActivator.getForName(name));
        if (nameBasedIndexFacetingPolicy.value().matches(name)) {
            return closeables.manage(new TaxonomyFacetedLuceneStorage(underlyingStorage, directoryActivator.value().apply(name + TAXONOMY_SUFFIX), new FacetsConfig(), fieldBasedFacetingPolicy));
        }

        return underlyingStorage;
    }

    @Override
    public void close() throws IOException {
        closeables.close();
    }
}

