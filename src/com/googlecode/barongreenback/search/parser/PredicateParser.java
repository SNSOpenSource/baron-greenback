package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.records.Record;

public interface PredicateParser {
    Predicate<Record> parse(String query);
}
