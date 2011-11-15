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
import static com.googlecode.totallylazy.Strings.contains;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.totallylazy.records.Keywords.keyword;

@SuppressWarnings("unchecked")
public class Grammar {
    public static final Parser<String> TEXT = isChar(CharacterPredicates.IS_ALPHA_NUMERIC).many1().source();
    public static final Parser<String> QUOTED_TEXT = notChar('"').many1().source().
            between(isChar('"'), isChar('"'));
    public static final Parser<String> VALUE = QUOTED_TEXT.or(TEXT);
    public static final Parser<String> NAME = VALUE;
    public static final Parser<Void> WILDCARD = isChar('*');

    public static final Parser<Predicate<String>> TEXT_STARTS_WITH = TEXT.followedBy(WILDCARD).map(new Callable1<String, Predicate<String>>() {
        public Predicate<String> call(String value) throws Exception {
            return startsWith(value);
        }
    });

    public static final Parser<Predicate<String>> TEXT_ENDS_WITH = Parsers.sequence(WILDCARD, TEXT).map(new Callable1<String, Predicate<String>>() {
        public Predicate<String> call(String value) throws Exception {
            return endsWith(value);
        }
    });

    public static final Parser<Predicate<String>> TEXT_CONTAINS = TEXT.between(WILDCARD, WILDCARD).map(new Callable1<String, Predicate<String>>() {
        public Predicate<String> call(String value) throws Exception {
            return contains(value);
        }
    });

    public static Parser<Callable1<Predicate, Predicate>> NEGATION() {
        return isChar('-').optional().map(new Callable1<Void, Callable1<Predicate, Predicate>>() {
            public Callable1<Predicate, Predicate> call(Void aVoid) throws Exception {
                return new Callable1<Predicate, Predicate>() {
                    public Predicate call(Predicate predicate) throws Exception {
                        return Predicates.not(predicate);
                    }
                };
            }
        });
    }

    public static final Parser<Predicate> TEXT_IS = VALUE.map(new Callable1<String, Predicate>() {
        public Predicate call(String value) throws Exception {
            return Predicates.is(value);
        }
    });

    public static final Parser<Predicate> VALUE_PREDICATE = Parsers.or(TEXT_CONTAINS, TEXT_STARTS_WITH, TEXT_ENDS_WITH, TEXT_IS).prefix(NEGATION());
    public static final Parser<List<Predicate>> VALUE_PREDICATES = VALUE_PREDICATE.sepBy(ws(","));

    private static Parser<Void> ws(String value) {
        return pattern(regex(String.format("\\s*%s\\s*", value)), value);
    }


    public static Parser<Predicate<Record>> VALUE_ONLY(final Sequence<Keyword> keywords) {
        return VALUE_PREDICATES.map(new Callable1<List<Predicate>, Predicate<Record>>() {
            public Predicate<Record> call(final List<Predicate> list) throws Exception {
                return or(keywords.map(new Callable1<Keyword, Predicate<Record>>() {
                    public Predicate<Record> call(final Keyword keyword) throws Exception {
                        return matchesValues(keyword, list);
                    }
                }).toArray(Predicate.class));
            }
        });
    }

    private static Predicate<Record> matchesValues(final Keyword keyword, List<Predicate> values) {
        return or(sequence(values).map(new Callable1<Predicate, Predicate<Record>>() {
            public Predicate<Record> call(Predicate predicate) throws Exception {
                return where(keyword, predicate);
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

    public static final Parser<Predicate<Record>> NAME_AND_VALUE = Parsers.tuple(NAME, ws(":"), VALUE_PREDICATES).map(new Callable1<Triple<String, Void, List<Predicate>>, Predicate<Record>>() {
        public Predicate<Record> call(Triple<String, Void, List<Predicate>> tuple) throws Exception {
            final String name = tuple.first();
            final List<Predicate> values = tuple.third();
            return matchesValues(keyword(name, String.class), values);
        }
    });

}
