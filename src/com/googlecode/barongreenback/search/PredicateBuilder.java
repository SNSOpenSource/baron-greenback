package com.googlecode.barongreenback.search;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class PredicateBuilder {

    private final PredicateParser queryParser;

    public PredicateBuilder(PredicateParser queryParser) {
        this.queryParser = queryParser;
    }

    public Either<String, Predicate<Record>> build(Model view, String query, Sequence<Keyword<?>> keywords) {
        return parse(prefix(view, query), keywords);
    }

    private static String prefix(Model view, final String query) {
        return sequence(queryFrom(view)).add(query).toString(" ");
    }

    private static String queryFrom(Model model) {
        return model.get("view", Model.class).get("query", String.class);
    }

    private Either<String, Predicate<Record>> parse(String query, Sequence<Keyword<?>> keywords) {
        try {
            Predicate<Record> predicate = queryParser.parse(query, keywords);
            return Either.right(predicate);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }
}
