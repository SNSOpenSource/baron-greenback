package com.googlecode.barongreenback.search;


import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.lazyrecords.Grammar;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.FixedClock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserFunctionsTest extends ApplicationTests {

    @Test
    public void shouldBeAbleToAddToDates() throws Exception {
        final Date now = new Date();
        setClockTo(new FixedClock(now));

        final Date threeHoursAgo = Dates.subtract(now, Calendar.HOUR, 3);

        PredicateParser parser = application.usingRequestScope(new Callable1<Container, PredicateParser>() {
            @Override
            public PredicateParser call(Container container) throws Exception {
                return container.get(PredicateParser.class);
            }
        });

        Keyword<Date> timeKeyword = Keyword.constructors.keyword("timeKeyword", Date.class);
        Predicate<Record> predicate = parser.parse("\"$addHours(now, \"-3\")$\"", Sequences.one(timeKeyword));

        Predicate<Record> expected = Predicates.where(timeKeyword, Grammar.is(threeHoursAgo));
        assertThat(predicate, is(expected));

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
