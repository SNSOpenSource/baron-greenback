package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

public interface PredicateParser {
    Predicate<Record> parse(String query, Sequence<? extends Keyword> implicits) throws IllegalArgumentException;
}
