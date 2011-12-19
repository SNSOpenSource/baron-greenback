package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.search.parser.PredicateParser;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import org.apache.lucene.queryParser.ParseException;

import static com.googlecode.barongreenback.shared.RecordDefinition.toKeywords;
import static com.googlecode.barongreenback.views.Views.recordName;
import static com.googlecode.barongreenback.views.Views.unwrap;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.records.Keywords.metadata;

public class RecordsService {

    private final Records records;
    private final ModelRepository modelRepository;
    private final PredicateParser queryParser;

    public RecordsService(final Records records, final ModelRepository modelRepository, final PredicateParser queryParser) {
        this.records = records;
        this.modelRepository = modelRepository;
        this.queryParser = queryParser;
    }

    public Number count(String viewName, String query) throws ParseException {
        Model view = view(viewName);
        final Keyword recordName = recordName(view);
        try {
            return records.get(recordName).filter(parse(prefix(view, query), Sequences.<Keyword>empty()).right()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void delete(String viewName, String query) {
        Model view = view(viewName);
        Keyword recordName = recordName(view);
        Predicate<Record> predicate = buildPredicate(query, view).right();
        records.remove(recordName, predicate);
    }

    private Either<String, Predicate<Record>> buildPredicate(String query, Model view) {
        Sequence<Keyword> headers = headers(view);
        Sequence<Keyword> visibleHeaders = visibleHeaders(headers);
        return parse(prefix(view, query), visibleHeaders);
    }

    public Record findUnique(String viewName, String query) {
        Model view = view(viewName);
        Keyword recordName = recordName(view);
        Sequence<Keyword> headers = headers(view);
        Predicate<Record> predicate = parse(prefix(view, query), headers).right();
        records.define(recordName, headers.toArray(Keyword.class));
        return records.get(recordName).filter(predicate).head();
    }

    public Either<String, Sequence<Record>> findAll(final String viewName, final String query) {
        final Option<Model> optionalView = findView(viewName);
        if (optionalView.isEmpty()) {
            return Either.right(Sequences.<Record>empty());
        } else {
            final Model view = optionalView.get();

            Either<String, Predicate<Record>> invalidQueryOrPredicate = buildPredicate(query, view);

            if (invalidQueryOrPredicate.isLeft()) {
                return Either.left(invalidQueryOrPredicate.left());
            } else {
                Keyword recordName = recordName(view);
                Sequence<Keyword> allHeaders = headers(view);
                records.define(recordName, allHeaders.toArray(Keyword.class));
                return Either.right(records.get(recordName).filter(invalidQueryOrPredicate.right()));
            }
        }
    }


    public Model view(String view) {
        return findView(view).get();
    }

    public Sequence<Keyword> visibleHeaders(final String viewName) {
        Sequence<Keyword> allHeaders = headers(view(viewName));
        return visibleHeaders(allHeaders);
    }

    public static Sequence<Keyword> headers(Model view) {
        return toKeywords(unwrap(view));
    }


    private static Sequence<Keyword> visibleHeaders(Sequence<Keyword> headers) {
        return headers.filter(where(metadata(Views.VISIBLE), is(notNullValue(Boolean.class).and(is(true)))));
    }

    private Option<Model> findView(String view) {
        return Views.find(modelRepository, view);
    }

    private static String prefix(Model view, final String query) {
        return sequence(queryFrom(view)).add(query).toString(" ");
    }

    private Either<String, Predicate<Record>> parse(String query, Sequence<Keyword> keywords) {
        try {
            Predicate<Record> predicate = queryParser.parse(query, keywords);
            return Either.right(predicate);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private static String queryFrom(Model model) {
        return model.get("view", Model.class).get("query", String.class);
    }
}
