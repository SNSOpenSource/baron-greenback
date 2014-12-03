package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.lazyrecords.lucene.CaseInsensitive;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.SearcherPool;
import com.googlecode.lazyrecords.lucene.TaxonomyFacetedLuceneStorage;
import com.googlecode.totallylazy.Function1;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.store.Directory;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.directoryActivatorFor;

public class NameBasedFacetedLuceneStorageActivator extends LuceneStorageActivator {

    public static final String TAXONOMY_SUFFIX = "-taxonomy";

    private final String persistenceUri;
    private final NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy;

    public NameBasedFacetedLuceneStorageActivator(PersistenceUri persistenceUri, NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy) {
        this.persistenceUri = persistenceUri.toString();
        this.nameBasedIndexFacetingPolicy = nameBasedIndexFacetingPolicy;
    }

    @Override
    public LuceneStorage call(String definition, Directory directory, SearcherPool searcherPool) throws Exception {
        final LuceneStorage underlyingStorage = CaseInsensitive.storage(directory, searcherPool);
        if (nameBasedIndexFacetingPolicy.value().matches(definition)) {
            final Function1<String, Directory> directoryActivator = directoryActivatorFor(persistenceUri);
            return new TaxonomyFacetedLuceneStorage(underlyingStorage, directoryActivator.call(definition + TAXONOMY_SUFFIX), new FacetsConfig());
        }
        return underlyingStorage;
    }
}
