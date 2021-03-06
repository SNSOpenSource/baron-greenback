package com.sky.sns.barongreenback.search;

import com.sky.sns.barongreenback.persistence.BaronGreenbackRecords;
import com.sky.sns.barongreenback.shared.ModelRepository;
import com.sky.sns.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.*;

import static com.sky.sns.barongreenback.shared.RecordDefinition.toKeywords;
import static com.sky.sns.barongreenback.views.ViewsRepository.unwrap;
import static com.sky.sns.barongreenback.views.ViewsRepository.viewName;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Callables.ignoreAndReturn;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.blank;
import static com.googlecode.totallylazy.Strings.format;

public class RecordsService {

    private final Records records;
    private final ModelRepository modelRepository;
    private final PredicateBuilder predicateBuilder;
    private final SearchFilter searchFilter;


    public RecordsService(final BaronGreenbackRecords records, final ModelRepository modelRepository, final PredicateBuilder predicateBuilder, SearchFilter searchFilter) {
        this.searchFilter = searchFilter;
        this.records = records.value();
        this.modelRepository = modelRepository;
        this.predicateBuilder = predicateBuilder;
    }

    public void delete(String viewName, String query, DrillDowns drillDowns) {
        Model view = view(viewName);
        Predicate<Record> predicate = predicateBuilder.build(prefixQueryWithFilters(view, query, searchFilter), headersForQuery(view), drillDowns).right();
        records.remove(Definition.constructors.definition(viewName(view), Sequences.<Keyword<?>>empty()), predicate);
    }

    public Number count(String viewName, String query) {
        return count(viewName, query, DrillDowns.empty());
    }

    public Number count(String viewName, String query, DrillDowns drillDowns) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return 0;

        Model view = optionalView.get();
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headersForQuery(view), drillDowns);
        return recordsFound.map(Callables.<String, Number>ignoreAndReturn(0), size());
    }

    private Callable1<Sequence<Record>, Number> size() {
        return new Callable1<Sequence<Record>, Number>() {
            @Override
            public Number call(Sequence<Record> records) throws Exception {
                return records.size();
            }
        };
    }

    public Option<Record> findUnique(String viewName, String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return none();

        Model view = optionalView.get();
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headersForQuery(view), DrillDowns.empty());
        return recordsFound.map(ignoreAndReturn(Option.none(Record.class)), firstResult());
    }


    public Either<String, Sequence<Record>> findFromView(final String viewName, final String query, DrillDowns drillDowns) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return Either.right(Sequences.<Record>empty());

        Model view = optionalView.get();
        return getRecords(view, query, headersForQuery(view), drillDowns);
    }

    public static String prefixQueryWithFilters(Model view, final String query, final SearchFilter searchFilter) {
        return sequence(queryFrom(view), query, searchFilter.getFilter()).filter(not(blank)).map(Strings.trim()).map(format("(%s)")).toString(" ");
    }


    public static String prefixQueryWithFilters(Model view, final String query) {
        return prefixQueryWithFilters(view, query, new DoNothingSearchFilter());
    }

    private static String queryFrom(Model model) {
        return model.get("view", Model.class).get("query", String.class);
    }

    private Either<String, Sequence<Record>> getRecords(Model view, String query, Sequence<Keyword<?>> visibleHeaders, DrillDowns drillDowns) {
        return getRecordsWithQuery(view, prefixQueryWithFilters(view, query,  searchFilter), visibleHeaders, drillDowns);
    }

    private Either<String, Sequence<Record>> getRecordsWithQuery(Model view, String query, Sequence<Keyword<?>> visibleHeaders, DrillDowns drillDowns) {
        Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(query, visibleHeaders, drillDowns);
        if (invalidQueryOrPredicate.isLeft()) return Either.left(invalidQueryOrPredicate.left());

        return right(getRecords(view, invalidQueryOrPredicate.right()));
    }

    public Sequence<Record> getRecords(Model view, Predicate<Record> predicate) {
        final Definition viewDefinition = definition(view);
        final Sequence<Keyword<?>> fields = viewDefinition.fields();
        final Definition unaliasedDefinition = Definition.constructors.definition(viewDefinition.name(), fields.map(unalias()));
        return records.get(unaliasedDefinition).filter(predicate);
    }

    public Definition definition(Model view) {
        return Definition.constructors.definition(viewName(view), headers(view));
    }

    public Model view(String view) {
        return findView(view).get();
    }

    public Option<Model> findView(String view) {
        return ViewsRepository.find(modelRepository, view);
    }

    public Sequence<Keyword<?>> visibleHeaders(final String viewName) {
        return visibleHeaders(view(viewName));
    }

    public static Sequence<Keyword<?>> visibleHeaders(final Model view) {
        return visibleHeaders(headers(view));
    }

    private static Sequence<Keyword<?>> visibleHeaders(Sequence<Keyword<?>> headers) {
        return headers.filter(where(metadata(ViewsRepository.VISIBLE), is(notNullValue(Boolean.class).and(is(true))))).realise();
    }

    public static Sequence<Keyword<?>> headers(Model view) {
        return toKeywords(unwrap(view));
    }

    private Sequence<Keyword<?>> headersForQuery(Model view) {
        return headers(view).map(unalias());
    }

    private static Callable1<Sequence<Record>, Option<Record>> firstResult() {
        return new Callable1<Sequence<Record>, Option<Record>>() {
            @Override
            public Option<Record> call(Sequence<Record> recordSequence) throws Exception {
                return recordSequence.headOption();
            }
        };
    }

    public static UnaryFunction<Keyword<?>> unalias() {
        return new UnaryFunction<Keyword<?>>() {
            @Override
            public Keyword<?> call(Keyword<?> keyword) throws Exception {
                if (keyword instanceof AliasedKeyword) {
                    return Unchecked.<AliasedKeyword<?>>cast(keyword).source().metadata(keyword.metadata());
                }
                return keyword;
            }
        };
    }
}
