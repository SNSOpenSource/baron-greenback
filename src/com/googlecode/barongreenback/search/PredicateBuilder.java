package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.List;

import static com.googlecode.lazyrecords.Keyword.functions.name;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.Predicates.alwaysTrue;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class PredicateBuilder {

    private final PredicateParser queryParser;
    private final StringMappings stringMappings;

    public PredicateBuilder(PredicateParser queryParser, BaronGreenbackStringMappings baronGreenbackStringMappings) {
        this.queryParser = queryParser;
        this.stringMappings = baronGreenbackStringMappings.value();
    }

    public Either<String, Predicate<Record>> build(String query, final Sequence<Keyword<?>> keywords) {
        return build(query, keywords, DrillDowns.empty());
    }

    public Either<String, Predicate<Record>> build(String query, final Sequence<Keyword<?>> keywords, final DrillDowns drillDowns) {
        final Either<String, Predicate<Record>> parsedQuery = parse(query, keywords);
        return parsedQuery.map(new UnaryFunction<Predicate<Record>>() {
            @Override
            public Predicate<Record> call(Predicate<Record> queryPredicate) throws Exception {
                return Predicates.and(queryPredicate, drilldownPredicate(keywords, drillDowns));
            }
        });
    }

    public Predicate<Record> drilldownPredicate(final Sequence<Keyword<?>> fields, DrillDowns drillDowns) {
        final boolean hasDrillDownNotInFields = pairs(drillDowns.value()).exists(where(Callables.first(String.class), not(in(fields.map(name)))));
        if(hasDrillDownNotInFields) return Predicates.alwaysFalse();
        return pairs(drillDowns.value()).map(Callables.<String, List<String>, Keyword<?>>first(nameToKeywordWith(fields)))
                .fold(alwaysTrue(Record.class), withDrilldownPredicate());
    }

    private Callable1<String, Keyword<?>> nameToKeywordWith(final Sequence<Keyword<?>> fields) {
        return new Mapper<String, Keyword<?>>() {
            @Override
            public Keyword<?> call(String drillDownName) throws Exception {
                return fields.find(where(name, is(drillDownName))).get();
            }
        };
    }

    private Either<String, Predicate<Record>> parse(String query, Sequence<Keyword<?>> keywords) {
        try {
            Predicate<Record> predicate = queryParser.parse(query, keywords);
            return Either.right(predicate);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private Function2<LogicalPredicate<Record>, Pair<Keyword<?>, List<String>>, LogicalPredicate<Record>> withDrilldownPredicate() {
        return new Function2<LogicalPredicate<Record>, Pair<Keyword<?>, List<String>>, LogicalPredicate<Record>>() {
            @Override
            public LogicalPredicate<Record> call(LogicalPredicate<Record> recordPredicate, Pair<Keyword<?>, List<String>> facetDrillDown) throws Exception {
                final Keyword<?> keyword = facetDrillDown.first();
                final Sequence<Object> facetValues = sequence(facetDrillDown.second()).map(new Mapper<String, Object>() {
                    @Override
                    public Object call(String valueAsString) throws Exception {
                        return stringMappings.toValue(keyword.forClass(), valueAsString);
                    }
                });
                Predicate<Record> facetPredicate = where(keyword, in(facetValues));
                return recordPredicate.and(facetPredicate);
            }
        };
    }


}
