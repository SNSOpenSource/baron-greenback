package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;


public class StandardParser implements PredicateParser {
    public Predicate<Record> parse(String raw, Sequence<? extends Keyword> implicits) {
        final String query = raw.trim();
        if(Strings.isEmpty(query)) {
            return Predicates.all();
        }
        return Grammar.PARSER(implicits.safeCast(Keyword.class)).parse(query);
    }
}
