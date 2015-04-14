package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.InMemoryPersistentTypes;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings.baronGreenbackStringMappings;
import static com.googlecode.barongreenback.search.DrillDowns.drillDowns;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PredicateBuilderTest {

    private static final Keyword<String> FOO = keyword("foo", String.class);

    @Test
    public void supportsEmptyDrilldown() throws Exception {
        final Either<String, Predicate<Record>> result = buildPredicateWith(DrillDowns.empty());

        assertThat(result.isRight(), is(true));
        assertThat(result.right().matches(record(FOO, "bar")), is(true));
        assertThat(result.right().matches(record(FOO, "bartholemew")), is(true));
    }

    @Test
    public void supportsSimpleDrilldown() throws Exception {
        final Either<String, Predicate<Record>> result = buildPredicateWith(drillDowns(singletonMap("foo", asList("bar"))));

        assertThat(result.isRight(), is(true));
        assertThat(result.right().matches(record(FOO, "bar")), is(true));
        assertThat(result.right().matches(record(FOO, "bartholemew")), is(false));
    }

    @Test
    public void supportsDrillingDownToMultipleValue() throws Exception {
        final Either<String, Predicate<Record>> result = buildPredicateWith(drillDowns(singletonMap("foo", asList("bar", "minion"))));

        assertThat(result.isRight(), is(true));
        assertThat(result.right().matches(record(FOO, "bar")), is(true));
        assertThat(result.right().matches(record(FOO, "minion")), is(true));
        assertThat(result.right().matches(record(FOO, "bartholemew")), is(false));
    }

    private Either<String, Predicate<Record>> buildPredicateWith(DrillDowns drillDowns) {
        return new PredicateBuilder(parserReturningTrue(), baronGreenbackStringMappings(new StringMappings(), new InMemoryPersistentTypes())).build("", Sequences.<Keyword<?>>one(FOO), drillDowns);
    }

    private PredicateParser parserReturningTrue() {
        return new PredicateParser() {
            @Override
            public Predicate<Record> parse(String query, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException {
                return Predicates.alwaysTrue();
            }
        };
    }

}