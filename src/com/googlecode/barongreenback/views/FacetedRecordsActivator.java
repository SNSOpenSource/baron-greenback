package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.lucene.NameBasedIndexFacetingPolicy;
import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.FacetedLuceneStorage;
import com.googlecode.lazyrecords.lucene.LuceneFacetedRecords;
import com.googlecode.lazyrecords.lucene.LuceneQueryPreprocessor;
import com.googlecode.lazyrecords.lucene.PartitionedIndex;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.QueryParameters;
import com.googlecode.utterlyidle.Request;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Unchecked.cast;

public class FacetedRecordsActivator implements Callable<FacetedRecords> {

    private final LuceneMappings luceneMappings;
    private final PartitionedIndex partitionedIndex;
    private final NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy;
    private final LuceneQueryPreprocessor luceneQueryPreprocessor;
    private final Request request;

    public FacetedRecordsActivator(BaronGreenbackApplicationScope baronGreenbackApplicationScope, BaronGreenbackRequestScope baronGreenbackRequestScope, Request request) {
        this.luceneMappings = baronGreenbackRequestScope.value().get(LuceneMappings.class);
        this.partitionedIndex = baronGreenbackApplicationScope.value().get(PartitionedIndex.class);
        this.nameBasedIndexFacetingPolicy = baronGreenbackApplicationScope.value().get(NameBasedIndexFacetingPolicy.class);
        this.luceneQueryPreprocessor = baronGreenbackRequestScope.value().get(LuceneQueryPreprocessor.class);
        this.request = request;
    }

    @Override
    public FacetedRecords call() throws Exception {
        final QueryParameters queryParameters = QueryParameters.parse(request.uri().query());
        final String definition = queryParameters.getValue("current");
        final Predicate<String> definitionPredicate = nameBasedIndexFacetingPolicy.value();
        if (definitionPredicate.matches(definition)) {
            final FacetedLuceneStorage facetedStorage = cast(partitionedIndex.partition(definition));
            return new LuceneFacetedRecords(facetedStorage, luceneMappings, luceneQueryPreprocessor);
        }
        return new FacetedRecords() {
            @Override
            public <T extends Pair<Keyword<?>, Integer>> Sequence<Facet<Facet.FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests) throws IOException {
                return Sequences.empty();
            }

            @Override
            public <T extends Pair<Keyword<?>, Integer>, S extends Group<Keyword<?>, String>> Sequence<Facet<Facet.FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests, Sequence<S> drillDowns) throws IOException {
                return Sequences.empty();
            }
        };
    }
}
