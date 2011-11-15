package com.googlecode.barongreenback.search.parser;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.Scanners;
import com.googlecode.lazyparsec.pattern.CharacterPredicates;
import com.googlecode.lazyparsec.pattern.Patterns;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import java.util.List;

import static com.googlecode.lazyparsec.Scanners.isChar;
import static com.googlecode.lazyparsec.Scanners.notChar;
import static com.googlecode.lazyparsec.Scanners.pattern;
import static com.googlecode.lazyparsec.pattern.Patterns.regex;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Predicates.or;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.records.Keywords.keyword;

@SuppressWarnings("unchecked")
public class Grammar {
    public static final Parser<String> TEXT = isChar(CharacterPredicates.IS_ALPHA_NUMERIC).many1().source();
    public static final Parser<String> QUOTED_TEXT = notChar('"').many1().source().
            between(isChar('"'), isChar('"'));
    public static final Parser<String> VALUE = QUOTED_TEXT.or(TEXT);
    public static final Parser<List<String>> VALUES = VALUE.sepBy(ws(","));

    private static Parser<Void> ws(String value) {
        return pattern(regex(String.format("\\s*%s\\s*", value)), value);
    }

    public static final Parser<String> NAME = VALUE;
    public static final Parser<Prefix> PREFIX = Scanners.among("+-").optional().source().map(new Callable1<String, Prefix>() {
        public Prefix call(String value) throws Exception {
            return Prefix.parse(value);
        }
    });

    public static Parser<Predicate<Record>> VALUE_ONLY(final Sequence<Keyword> keywords) {
        return Parsers.pair(PREFIX, VALUES).map(new Callable1<Pair<Prefix, List<String>>, Predicate<Record>>() {
            public Predicate<Record> call(final Pair<Prefix, List<String>> pair) throws Exception {
                return or(keywords.map(new Callable1<Keyword, Predicate>() {
                    public Predicate call(final Keyword keyword) throws Exception {
                        return matchesValues(keyword, pair.second(), asCallable(pair.first()));
                    }
                }).toArray(Predicate.class));
            }
        });
    }

    private static Callable1<String, Predicate<String>> asCallable(final Prefix prefix) {
        return new Callable1<String, Predicate<String>>() {
            public Predicate<String> call(String value) throws Exception {
                if (prefix.equals(Prefix.Minus)) {
                    return Predicates.not(value);
                }
                return Predicates.is(value);
            }
        };
    }

    private static LogicalPredicate matchesValues(final Keyword keyword, List<String> values, final Callable1<String, Predicate<String>> predicate) {
        return or(sequence(values).map(new Callable1<String, Predicate>() {
            public Predicate call(String value) throws Exception {
                return where(keyword, predicate.call(value));
            }
        }).toArray(Predicate.class));
    }

    public static Parser<Predicate<Record>> PARTS(final Sequence<Keyword> keywords) {
        return Parsers.or(NAME_AND_VALUE, VALUE_ONLY(keywords));
    }

    public static Parser<Predicate<Record>> PARSER(final Sequence<Keyword> keywords) {
        return PARTS(keywords).followedBy(isChar(' ').many().optional()).many().map(new Callable1<List<Predicate<Record>>, Predicate<Record>>() {
            public Predicate<Record> call(final List<Predicate<Record>> predicates) throws Exception {
                return and(predicates.toArray(new Predicate[0]));
            }
        });
    }

    public static final Parser<Predicate<Record>> NAME_AND_VALUE = Parsers.tuple(PREFIX, NAME, ws(":"), VALUES).map(new Callable1<Quadruple<Prefix, String, Void, List<String>>, Predicate<Record>>() {
        public Predicate<Record> call(Quadruple<Prefix, String, Void, List<String>> tuple) throws Exception {
            final Prefix prefix = tuple.first();
            final String name = tuple.second();
            final List<String> values = tuple.fourth();
            return matchesValues(keyword(name, String.class), values, asCallable(prefix));
        }
    });

}
