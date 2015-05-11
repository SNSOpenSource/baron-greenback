package sky.sns.barongreenback.views;

import sky.sns.barongreenback.persistence.lucene.NameBasedIndexFacetingPolicy;
import sky.sns.barongreenback.shared.BaronGreenbackApplicationScope;
import sky.sns.barongreenback.shared.BaronGreenbackRequestScope;
import sky.sns.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
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
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Unchecked.cast;

public class FacetedRecordsActivator implements Callable<FacetedRecords> {

    private final LuceneMappings luceneMappings;
    private final PartitionedIndex partitionedIndex;
    private final NameBasedIndexFacetingPolicy nameBasedIndexFacetingPolicy;
    private final LuceneQueryPreprocessor luceneQueryPreprocessor;
    private final ModelRepository modelRepository;
    private final CurrentView currentView;

    public FacetedRecordsActivator(BaronGreenbackApplicationScope baronGreenbackApplicationScope, BaronGreenbackRequestScope baronGreenbackRequestScope, ModelRepository modelRepository, CurrentView currentView) {
        this.modelRepository = modelRepository;
        this.luceneMappings = baronGreenbackRequestScope.value().get(LuceneMappings.class);
        this.partitionedIndex = baronGreenbackApplicationScope.value().get(PartitionedIndex.class);
        this.nameBasedIndexFacetingPolicy = baronGreenbackApplicationScope.value().get(NameBasedIndexFacetingPolicy.class);
        this.luceneQueryPreprocessor = baronGreenbackRequestScope.value().get(LuceneQueryPreprocessor.class);
        this.currentView = currentView;
    }

    @Override
    public FacetedRecords call() throws Exception {
        final Option<Model> view = ViewsRepository.find(modelRepository, currentView.value());
        final Predicate<String> definitionPredicate = nameBasedIndexFacetingPolicy.value();
        if (view.isDefined() && definitionPredicate.matches(ViewsRepository.viewName(view.get()))) {
            final FacetedLuceneStorage facetedStorage = cast(partitionedIndex.partition(ViewsRepository.viewName(view.get())));
            return new LuceneFacetedRecords(facetedStorage, luceneMappings, luceneQueryPreprocessor);
        }
        return new EmptyFacetedRecords();
    }

    public static class EmptyFacetedRecords implements FacetedRecords {
        @Override
        public <T extends Pair<Keyword<?>, Integer>> Sequence<Facet<Facet.FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests) throws IOException {
            return Sequences.empty();
        }

        @Override
        public <T extends Pair<Keyword<?>, Integer>, S extends Group<Keyword<?>, String>> Sequence<Facet<Facet.FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests, Sequence<S> drillDowns) throws IOException {
            return Sequences.empty();
        }
    }
}
