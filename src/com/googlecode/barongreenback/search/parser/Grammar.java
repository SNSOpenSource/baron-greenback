package com.googlecode.barongreenback.search.parser;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.pattern.CharacterPredicates;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.time.Dates;

import java.util.Date;
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
    private static Parser<Void> ws(char value) {
        return ws(String.valueOf(value));
    }

    private static Parser<Void> ws(String value) {
        return pattern(regex(String.format("\\s*%s\\s*", value)), value);
    }

    public static final Parser<Date> DATE = pattern(regex("\\d{4}/\\d{1,2}/\\d{1,2}"), "date").source().map(new Callable1<String, Date>() {
        public Date call(String value) throws Exception {
            return Dates.format("yyyy/MM/dd").parse(value);
        }
    });
    public static final Parser<String> TEXT = isChar(CharacterPredicates.IS_ALPHA_NUMERIC).many1().source();
    public static final Parser<String> QUOTED_TEXT = notChar('"').many1().source().between(isChar('"'), isChar('"'));
    public static final Parser<String> TEXT_ONLY = Parsers.or(QUOTED_TEXT, TEXT);
    public static final Parser<Comparable> VALUES = Parsers.<Comparable>or(DATE, TEXT_ONLY);
    public static final Parser<String> NAME = TEXT_ONLY;
    public static final Parser<Void> WILDCARD = isChar('*');
    public static final Parser<Void> GT = ws('>');
    public static final Parser<Void> LT = ws('<');

    public static final Parser<Pair<Class, Predicate>> TEXT_STARTS_WITH = TEXT.followedBy(WILDCARD).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, startsWith(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_ENDS_WITH = Parsers.sequence(WILDCARD, TEXT).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, endsWith(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_CONTAINS = TEXT.between(WILDCARD, WILDCARD).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, contains(value));
        }
    });

    public static Parser<Callable1<Pair<Class, Predicate>, Pair<Class, Predicate>>> NEGATION() {
        return isChar('-').optional().map(new Callable1<Void, Callable1<Pair<Class, Predicate>, Pair<Class, Predicate>>>() {
            public Callable1<Pair<Class, Predicate>, Pair<Class, Predicate>> call(Void aVoid) throws Exception {
                return new Callable1<Pair<Class, Predicate>, Pair<Class, Predicate>>() {
                    public Pair<Class, Predicate> call(Pair<Class, Predicate> pair) throws Exception {
                        return Pair.<Class, Predicate>pair(pair.first(), Predicates.not(pair.second()));
                    }
                };
            }
        });
    }

    public static final Parser<Pair<Class, Predicate>> DATE_IS = DATE.map(new Callable1<Date, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Date dateWithoutTime) throws Exception {
            Date upper = Dates.addSeconds(dateWithoutTime, (24 * 60 * 60) - 1);
            return Pair.<Class, Predicate>pair(Date.class, Predicates.between(dateWithoutTime, upper));
        }
    });

    public static final Parser<Pair<Class, Predicate>> GREATER_THAN = Parsers.sequence(GT, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.greaterThan(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> LESS_THAN = Parsers.sequence(LT, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.lessThan(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_IS = TEXT_ONLY.map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, Predicates.is(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> VALUE_PREDICATE = Parsers.or(GREATER_THAN, LESS_THAN, DATE_IS, TEXT_CONTAINS, TEXT_STARTS_WITH, TEXT_ENDS_WITH, TEXT_IS).prefix(NEGATION());
    public static final Parser<List<Pair<Class, Predicate>>> VALUE_PREDICATES = VALUE_PREDICATE.sepBy(ws(','));

    public static Parser<Predicate<Record>> VALUE_ONLY(final Sequence<Keyword> keywords) {
        return VALUE_PREDICATES.map(new Callable1<List<Pair<Class, Predicate>>, Predicate<Record>>() {
            public Predicate<Record> call(final List<Pair<Class, Predicate>> list) throws Exception {
                return or(keywords.map(new Callable1<Keyword, Predicate<Record>>() {
                    public Predicate<Record> call(final Keyword keyword) throws Exception {
                        return matchesValues(keyword.name(), list);
                    }
                }).toArray(Predicate.class));
            }
        });
    }

    private static Predicate<Record> matchesValues(final String name, List<Pair<Class, Predicate>> values) {
        return or(sequence(values).map(new Callable1<Pair<Class, Predicate>, Predicate<Record>>() {
            public Predicate<Record> call(Pair<Class, Predicate> pair) throws Exception {
                return where(keyword(name, pair.first()), pair.second());
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

    public static final Parser<Predicate<Record>> NAME_AND_VALUE = Parsers.tuple(NAME, ws(':'), VALUE_PREDICATES).map(new Callable1<Triple<String, Void, List<Pair<Class, Predicate>>>, Predicate<Record>>() {
        public Predicate<Record> call(Triple<String, Void, List<Pair<Class, Predicate>>> tuple) throws Exception {
            final String name = tuple.first();
            final List<Pair<Class, Predicate>> values = tuple.third();
            return matchesValues(name, values);
        }
    });

}
