package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import org.apache.lucene.queryParser.ParseException;

import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.views.Views.recordName;
import static com.googlecode.barongreenback.views.Views.unwrap;
import static com.googlecode.totallylazy.Callables.ignoreAndReturn;
import static com.googlecode.totallylazy.Callables.size;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.lazyrecords.Keywords.metadata;

public class RecordsService {

    private final Records records;
    private final ModelRepository modelRepository;
    private final PredicateBuilder predicateBuilder;

    public RecordsService(final Records records, final ModelRepository modelRepository, final PredicateBuilder predicateBuilder) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.predicateBuilder = predicateBuilder;
    }

    public void delete(String viewName, String query) {
        Model view = view(viewName);
        Predicate<Record> predicate = predicateBuilder.build(view, query, visibleHeaders(view)).right();
        records.remove(recordName(view), predicate);
    }

    public Integer count(String viewName, String query) throws ParseException {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return 0;

        Model view = optionalView.get();
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headers(view));
        return recordsFound.map(Callables.<String, Integer>ignoreAndReturn(0), size());
    }

    public Option<Record> findUnique(String viewName, String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return none();

        Model view = optionalView.get();
        Either<String, Sequence<Record>> recordsFound = getRecords(view, query, headers(view));
        return recordsFound.map(ignoreAndReturn(Option.none(Record.class)), firstResult());
    }

    public Either<String, Sequence<Record>> findAll(final String viewName, final String query) {
        Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) return Either.right(Sequences.<Record>empty());

        Model view = optionalView.get();
        return getRecords(view, query, visibleHeaders(headers(view)));
    }

    private Either<String, Sequence<Record>> getRecords(Model view, String query, Sequence<Keyword> visibleHeaders) {
        Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(view, query, visibleHeaders);
        if(invalidQueryOrPredicate.isLeft()) return Either.left(invalidQueryOrPredicate.left());

        Keyword recordName = recordName(view);
        records.define(recordName, headers(view).toArray(Keyword.class));
        return right(records.get(recordName).filter(invalidQueryOrPredicate.right()));
    }

    public Model view(String view) {
        return findView(view).get();
    }

    private Option<Model> findView(String view) {
        return Views.find(modelRepository, view);
    }

    public Sequence<Keyword> visibleHeaders(final String viewName) {
        return visibleHeaders(view(viewName));
    }

    public Sequence<Keyword> visibleHeaders(final Model view) {
        return visibleHeaders(headers(view));
    }

    private static Sequence<Keyword> visibleHeaders(Sequence<Keyword> headers) {
        return headers.filter(where(metadata(Views.VISIBLE), is(notNullValue(Boolean.class).and(is(true)))));
    }


    public static Sequence<Keyword> headers(Model view) {
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
