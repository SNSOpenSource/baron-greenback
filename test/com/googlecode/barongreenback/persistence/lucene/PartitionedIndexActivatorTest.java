package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.PersistenceUri;
import com.googlecode.totallylazy.Files;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static com.googlecode.barongreenback.persistence.lucene.NameBasedFacetedLuceneStorageActivator.TAXONOMY_SUFFIX;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.Predicates.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PartitionedIndexActivatorTest {
    private static final String indexLocation = "/tmp/" + PartitionedIndexActivatorTest.class.getSimpleName() + "-index";
    private static final NameBasedIndexFacetingPolicy NO_FACETED_STORAGE_POLICY = new NameBasedIndexFacetingPolicy(alwaysFalse(String.class));
    private static final NameBasedIndexFacetingPolicy WITH_FACETED_STORAGE_POLICY = new NameBasedIndexFacetingPolicy(is("shard"));

    @After
    public void cleanup() {
        Files.delete(new File(indexLocation));
    }

    @Test
    public void canCreateInMemoryIndex() throws Exception {
        final PersistenceUri persistenceUri = new PersistenceUri("lucene:mem");
        final NameBasedFacetedLuceneStorageActivator luceneStorageActivator = new NameBasedFacetedLuceneStorageActivator(persistenceUri, NO_FACETED_STORAGE_POLICY);
        new PartitionedIndexActivator(persistenceUri, luceneStorageActivator).call();
    }

    @Test
    public void canCreateNioIndexWithoutTaxonomyIndex() throws Exception {
        final PersistenceUri persistenceUri = new PersistenceUri("lucene:nio://" + indexLocation);
        final NameBasedFacetedLuceneStorageActivator luceneStorageActivator = new NameBasedFacetedLuceneStorageActivator(persistenceUri, NO_FACETED_STORAGE_POLICY);
        new PartitionedIndexActivator(persistenceUri, luceneStorageActivator).call().partition("shard");
        assertTrue(new File(indexLocation + "/shard").exists());
        assertFalse(new File(indexLocation + "/shard" + TAXONOMY_SUFFIX).exists());
    }

    @Test
    public void canCreateNioIndexWithoutTaxonomyIndexIfNotEnabledForAShard() throws Exception {
        final PersistenceUri persistenceUri = new PersistenceUri("lucene:nio://" + indexLocation);
        final NameBasedFacetedLuceneStorageActivator luceneStorageActivator = new NameBasedFacetedLuceneStorageActivator(persistenceUri, WITH_FACETED_STORAGE_POLICY);
        new PartitionedIndexActivator(persistenceUri, luceneStorageActivator).call().partition("different_shard");
        assertTrue(new File(indexLocation + "/different_shard").exists());
        assertFalse(new File(indexLocation + "/different_shard" + TAXONOMY_SUFFIX).exists());
    }

    @Test
    public void canCreateNioIndexWithTaxonomyIndex() throws Exception {
        final PersistenceUri persistenceUri = new PersistenceUri("lucene:nio://" + indexLocation);
        final NameBasedFacetedLuceneStorageActivator luceneStorageActivator = new NameBasedFacetedLuceneStorageActivator(persistenceUri, WITH_FACETED_STORAGE_POLICY);
        new PartitionedIndexActivator(new PersistenceUri("lucene:nio://" + indexLocation), luceneStorageActivator).call().partition("shard");
        assertTrue(new File(indexLocation + "/shard").exists());
        assertTrue(new File(indexLocation + "/shard" + TAXONOMY_SUFFIX).exists());
    }

    @Test
    public void canCreateFileIndex() throws Exception {
        final PersistenceUri persistenceUri = new PersistenceUri("lucene:file://" + indexLocation);
        final NameBasedFacetedLuceneStorageActivator luceneStorageActivator = new NameBasedFacetedLuceneStorageActivator(persistenceUri, WITH_FACETED_STORAGE_POLICY);
        new PartitionedIndexActivator(persistenceUri, luceneStorageActivator).call();
    }
}
