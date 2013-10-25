package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.views.ViewsRepository.unwrap;
import static com.googlecode.barongreenback.views.ViewsRepository.viewName;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Callables.ignoreAndReturn;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class RecordsService {

    private final Records records;
    private final ModelRepository modelRepository;
    private final PredicateBuilder predicateBuilder;

    public RecordsService(final BaronGreenbackRecords records, final ModelRepository modelRepository, final PredicateBuilder predicateBuilder) {
        this.records = records.value();
        this.modelRepository = modelRepository;
        this.predicateBuilder = predicateBuilder;
    }

    public void delete(String viewName, String query) {
        Model view = view(viewName);
        Predicate<Record> predicate = predicateBuilder.build(prefixQueryWithImplicitViewQuery(view, query), headers(view)).right();
        records.remove(Definition.constructors.definition(viewName(view), Sequences.<Keyword<?>>empty()), predicate);
    }

    public Number count(String viewName, String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return 0;

        Model view = optionalView.get();
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headers(view));
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
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headers(view));
        return recordsFound.map(ignoreAndReturn(Option.none(Record.class)), firstResult());
    }

    public Either<String, Sequence<Record>> findAll(String viewName, String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return Either.right(Sequences.<Record>empty());

        Model view = optionalView.get();
        return getRecordsWithQuery(view, query, headers(view));
    }

    public Either<String, Sequence<Record>> findFromView(final String viewName, final String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return Either.right(Sequences.<Record>empty());

        Model view = optionalView.get();
        return getRecords(view, query, headers(view));
    }

    public static String prefixQueryWithImplicitViewQuery(Model view, final String query) {
        return sequence(queryFrom(view)).append(query != null ? query : "").toString(" ");
    }

    private static String queryFrom(Model model) {
        return model.get("view", Model.class).get("query", String.class);
    }

    private Either<String, Sequence<Record>> getRecords(Model view, String query, Sequence<Keyword<?>> visibleHeaders) {
        return getRecordsWithQuery(view, prefixQueryWithImplicitViewQuery(view, query), visibleHeaders);
    }

    private Either<String, Sequence<Record>> getRecordsWithQuery(Model view, String query, Sequence<Keyword<?>> visibleHeaders) {
        Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(query, visibleHeaders);
        if (invalidQueryOrPredicate.isLeft()) return Either.left(invalidQueryOrPredicate.left());

        return right(getRecords(view, invalidQueryOrPredicate.right()));
    }

    public Sequence<Record> getRecords(Model view, Predicate<Record> predicate) {
        Definition viewDefinition = definition(view);
        return records.get(viewDefinition).filter(predicate);
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

    private static Callable1<Sequence<Record>, Option<Record>> firstResult() {
        return new Callable1<Sequence<Record>, Option<Record>>() {
            @Override
            public Option<Record> call(Sequence<Record> recordSequence) throws Exception {
                return recordSequence.headOption();
            }
        };
    }
}
