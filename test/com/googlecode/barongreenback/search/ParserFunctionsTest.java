package com.googlecode.barongreenback.search;


import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.lazyrecords.Grammar;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.parser.ParserParameters;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.FixedClock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserFunctionsTest extends ApplicationTests {

    @Test
    public void shouldBeAbleToAddToDates() throws Exception {
        final Date now = new Date();
        setClockTo(new FixedClock(now));
        final Date over3HoursAgo = new Date(now.getTime() - (3 * 3600 * 1000) );

        application.usingRequestScope(new Callable1<Container, String>() {
            @Override
            public String call(Container container) throws Exception {
                PredicateParser parser = container.get(PredicateParser.class);

                Keyword<Date> timeKeyword = Keyword.constructors.keyword("timeKeyword", Date.class);
                Predicate<Record> predicate = parser.parse("timeKeyword : \"$addHours(now, \"-3\")$\"", Sequences.one(timeKeyword));

                Predicate<Record> expected = Predicates.where(timeKeyword, Grammar.is(over3HoursAgo));
                assertThat(predicate, is(expected));

                return null;
            }
        });
    }


    private Application setClockTo(final Clock myClock) {
        return application.add(new ApplicationScopedModule() {
            @Override
            public Container addPerApplicationObjects(Container container) throws Exception {
                container.remove(Clock.class);
                container.addInstance(Clock.class, myClock);
                return container;
            }
        });
    }
}
