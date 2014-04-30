package com.googlecode.barongreenback.search;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

public class PredicateBuilder {

    private final PredicateParser queryParser;

    public PredicateBuilder(PredicateParser queryParser) {
        this.queryParser = queryParser;
    }

    public Either<String, Predicate<Record>> build(String query, Sequence<Keyword<?>> keywords) {
        return parse(query, keywords);
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
