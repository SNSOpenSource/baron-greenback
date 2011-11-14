package com.googlecode.barongreenback.search.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;


public class StandardParser implements PredicateParser {
    private final Sequence<Keyword> keywords;

    public StandardParser(Sequence<Keyword> keywords) {
        this.keywords = keywords;
    }

    public StandardParser(Keyword... keywords) {
        this.keywords = Sequences.sequence(keywords);
    }

    public Predicate<Record> parse(String query) {
        if(Strings.isEmpty(query)) {
            return Predicates.all();
        }
        return Grammar.PARSER(keywords).parse(query);
    }
}
