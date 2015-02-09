package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.lazyrecords.lucene.FieldBasedFacetingPolicy;
import com.googlecode.lazyrecords.lucene.LucenePartitionedIndex;
import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import org.apache.lucene.store.Directory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static com.googlecode.barongreenback.persistence.lucene.LucenePersistence.directoryActivatorFor;
import static com.googlecode.barongreenback.persistence.lucene.TaxonomyNameToLuceneStorageFunction.TAXONOMY_SUFFIX;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.Predicates.is;
import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TaxonomyLuceneStorageFactoryTest {
    private static String indexLocation;
    private static final NameBasedIndexFacetingPolicy NO_FACETED_STORAGE_POLICY = new NameBasedIndexFacetingPolicy(alwaysFalse(String.class));
    private static final NameBasedIndexFacetingPolicy WITH_FACETED_STORAGE_POLICY = new NameBasedIndexFacetingPolicy(is("shard"));
    private static final FieldBasedFacetingPolicy NO_FACETED_FIELDS_POLICY = new FieldBasedFacetingPolicy(alwaysFalse(String.class));

    @Before
    public void createTempIndexDirectory() throws Exception {
        final Path tempDirectory = createTempDirectory("index");
        indexLocation = tempDirectory.toString();
    }

    @After
    public void cleanup() {
        Files.delete(new File(indexLocation));
    }

    @Test
    public void canCreateInMemoryIndex() throws Exception {
        final NameToLuceneDirectoryFunction luceneDirectoryActivator = new NameToLuceneDirectoryFunction(directoryActivatorFor("lucene:mem"));
        final CaseInsensitiveNameToLuceneStorageFunctionActivator storageFunction = new CaseInsensitiveNameToLuceneStorageFunctionActivator(luceneDirectoryActivator);
        final TaxonomyNameToLuceneStorageFunction taxonomyStorageFunction = new TaxonomyNameToLuceneStorageFunction(storageFunction.call(), luceneDirectoryActivator, NO_FACETED_STORAGE_POLICY, NO_FACETED_FIELDS_POLICY);
        final LucenePartitionedIndex lucenePartitionedIndex = new LucenePartitionedIndex(taxonomyStorageFunction);
        final LuceneStorage storage = lucenePartitionedIndex.partition("shard");
        assertThat(storage, CoreMatchers.<LuceneStorage>notNullValue());
    }

    @Test
    public void canCreateNioIndexWithoutTaxonomyIndex() throws Exception {
        final Function1<String, Directory> directoryActivator = directoryActivatorFor("lucene:nio://" + indexLocation);
        final NameToLuceneDirectoryFunction luceneDirectoryActivator = new NameToLuceneDirectoryFunction(directoryActivator);
        final CaseInsensitiveNameToLuceneStorageFunctionActivator storageFunction = new CaseInsensitiveNameToLuceneStorageFunctionActivator(luceneDirectoryActivator);
        final TaxonomyNameToLuceneStorageFunction taxonomyStorageFunction = new TaxonomyNameToLuceneStorageFunction(storageFunction.call(), luceneDirectoryActivator, NO_FACETED_STORAGE_POLICY, NO_FACETED_FIELDS_POLICY);
        new LucenePartitionedIndex(taxonomyStorageFunction).partition("shard");
        assertTrue(new File(indexLocation + "/shard").exists());
        assertFalse(new File(indexLocation + "/shard" + TAXONOMY_SUFFIX).exists());
    }

    @Test
    public void canCreateNioIndexWithoutTaxonomyIndexIfNotEnabledForAShard() throws Exception {
        final Function1<String, Directory> directoryActivator = directoryActivatorFor("lucene:nio://" + indexLocation);
        final NameToLuceneDirectoryFunction luceneDirectoryActivator = new NameToLuceneDirectoryFunction(directoryActivator);
        final CaseInsensitiveNameToLuceneStorageFunctionActivator storageFunction = new CaseInsensitiveNameToLuceneStorageFunctionActivator(luceneDirectoryActivator);
        final TaxonomyNameToLuceneStorageFunction taxonomyStorageFunction = new TaxonomyNameToLuceneStorageFunction(storageFunction.call(), luceneDirectoryActivator, WITH_FACETED_STORAGE_POLICY, NO_FACETED_FIELDS_POLICY);
        new LucenePartitionedIndex(taxonomyStorageFunction).partition("different_shard");
        assertTrue(new File(indexLocation + "/different_shard").exists());
        assertFalse(new File(indexLocation + "/different_shard" + TAXONOMY_SUFFIX).exists());
    }

    @Test
    public void canCreateNioIndexWithTaxonomyIndex() throws Exception {
        final Function1<String, Directory> directoryActivator = directoryActivatorFor("lucene:nio://" + indexLocation);
        final NameToLuceneDirectoryFunction luceneDirectoryActivator = new NameToLuceneDirectoryFunction(directoryActivator);
        final CaseInsensitiveNameToLuceneStorageFunctionActivator storageFunction = new CaseInsensitiveNameToLuceneStorageFunctionActivator(luceneDirectoryActivator);
        final TaxonomyNameToLuceneStorageFunction taxonomyStorageFunction = new TaxonomyNameToLuceneStorageFunction(storageFunction.call(), luceneDirectoryActivator, WITH_FACETED_STORAGE_POLICY, NO_FACETED_FIELDS_POLICY);
        new LucenePartitionedIndex(taxonomyStorageFunction).partition("shard");
        assertTrue(new File(indexLocation + "/shard").exists());
        assertTrue(new File(indexLocation + "/shard" + TAXONOMY_SUFFIX).exists());
    }
}
