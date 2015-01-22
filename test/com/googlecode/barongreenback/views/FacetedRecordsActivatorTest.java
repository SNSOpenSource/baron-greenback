package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.barongreenback.persistence.lucene.NameBasedIndexFacetingPolicy;
import com.googlecode.barongreenback.persistence.lucene.TaxonomyNameToLuceneStorageFunction;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.lucene.ClosingNameToLuceneStorageFunction;
import com.googlecode.lazyrecords.lucene.FieldBasedFacetingPolicy;
import com.googlecode.lazyrecords.lucene.LuceneFacetedRecords;
import com.googlecode.lazyrecords.lucene.LucenePartitionedIndex;
import com.googlecode.lazyrecords.lucene.LuceneQueryPreprocessor;
import com.googlecode.lazyrecords.lucene.NameToLuceneDirectoryFunction;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Functions;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Request;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThat;

public class FacetedRecordsActivatorTest {

    private BaronGreenbackApplicationScope baronGreenbackApplicationScope;
    private BaronGreenbackRequestScope baronGreenbackRequestScope;
    private ModelRepository modelRepository;

    @Before
    public void setup() throws Exception {
        facetNews();
        createBoozeView();
    }

    private void facetNews() throws Exception {
        final String definitionName = "news";
        final NameBasedIndexFacetingPolicy facetingPolicy = new NameBasedIndexFacetingPolicy(Predicates.is(definitionName));
        final FieldBasedFacetingPolicy fieldBasedFacetingPolicy = new FieldBasedFacetingPolicy(Predicates.in("title", "description", "link", "pubDate"));
        final NameToLuceneDirectoryFunction nameToLuceneDirectoryFunction = new NameToLuceneDirectoryFunction(Functions.<String, Directory>returns1(new RAMDirectory()));
        final TaxonomyNameToLuceneStorageFunction storageActivator = new TaxonomyNameToLuceneStorageFunction(new ClosingNameToLuceneStorageFunction(nameToLuceneDirectoryFunction, new KeywordAnalyzer()), nameToLuceneDirectoryFunction, facetingPolicy, fieldBasedFacetingPolicy);

        final Container parentScope = new SimpleContainer().addInstance(LuceneMappings.class, new LuceneMappings()).
                addInstance(NameBasedIndexFacetingPolicy.class, facetingPolicy).
                addInstance(PartitionedIndex.class, new LucenePartitionedIndex(storageActivator)).
                addInstance(LuceneQueryPreprocessor.class, null).
                addInstance(Request.class, null);

        baronGreenbackApplicationScope = new BaronGreenbackApplicationScope(parentScope);
        baronGreenbackApplicationScope.value().addInstance(BaronGreenbackApplicationScope.class, baronGreenbackApplicationScope);
        baronGreenbackRequestScope = new BaronGreenbackRequestScope(baronGreenbackApplicationScope.value());
        modelRepository = new RecordsModelRepository(BaronGreenbackRecords.records(new MemoryRecords(new StringMappings().add(Model.class, new ModelMapping()))));
    }

    private void createBoozeView() {
        new ViewsRepository(modelRepository).set(randomUUID(), ViewsRepository.viewModel(Sequences.<Keyword<?>>empty(), "booze", "news", "", true, ""));
    }

    @Test
    public void shouldNotUseViewNameForPolicy() throws Exception {
        final FacetedRecords facetedRecords = activateFacetedRecordsWith("news");

        assertThat(facetedRecords, Matchers.instanceOf(FacetedRecordsActivator.EmptyFacetedRecords.class));
    }

    @Test
    public void shouldUseShardNameFromViewForPolicy() throws Exception {
        final FacetedRecords facetedRecords = activateFacetedRecordsWith("booze");

        assertThat(facetedRecords, Matchers.instanceOf(LuceneFacetedRecords.class));

    }
    private FacetedRecords activateFacetedRecordsWith(String viewName) throws Exception {
        return new FacetedRecordsActivator(baronGreenbackApplicationScope, baronGreenbackRequestScope, modelRepository, new CurrentView(viewName)).call();
    }

}