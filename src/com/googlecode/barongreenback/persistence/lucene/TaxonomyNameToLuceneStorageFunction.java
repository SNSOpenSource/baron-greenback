package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;
import com.googlecode.lazyrecords.lucene.NameToLuceneStorageFunction;
import com.googlecode.lazyrecords.lucene.TaxonomyFacetedLuceneStorage;
import com.googlecode.totallylazy.CloseableList;
import org.apache.lucene.facet.FacetsConfig;

import java.io.IOException;

public class TaxonomyNameToLuceneStorageFunction implements NameToLuceneStorageFunction {
    public static final String TAXONOMY_SUFFIX = "-taxonomy";

    private NameToLuceneStorageFunction underlyingStorageActivator;
    private NameToLuceneDirectoryFunction directoryActivator;

    private CloseableList closeables = new CloseableList();
    private NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy;

    public TaxonomyNameToLuceneStorageFunction(NameToLuceneStorageFunction underlyingStorageActivator, NameToLuceneDirectoryFunction directoryActivator, NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy) {
        this.underlyingStorageActivator = underlyingStorageActivator;
        this.directoryActivator = directoryActivator;
        this.nameBasedIndexFacetingPolicy = nameBasedIndexFacetingPolicy;
    }

    @Override
    public LuceneStorage getForName(String name) {
        final LuceneStorage underlyingStorage = closeables.manage(underlyingStorageActivator.getForName(name));
        if (nameBasedIndexFacetingPolicy.value().matches(name)) {
            return closeables.manage(new TaxonomyFacetedLuceneStorage(underlyingStorage, directoryActivator.value().apply(name + TAXONOMY_SUFFIX), new FacetsConfig()));
        }

        return underlyingStorage;
    }

    @Override
    public void close() throws IOException {
        closeables.close();
    }
}

