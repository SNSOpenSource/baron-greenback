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
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.FixedClock;
import com.googlecode.totallylazy.time.Hours;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;

public class ParserFunctionsTest extends ApplicationTests {

    private Keyword<Date> timeKeyword = Keyword.constructors.keyword("timeKeyword", Date.class);

    @Override
    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("some.property.value.of.three", "3");
        return properties;
    }

    @Test
    public void shouldBeAbleToAddToDates() throws Exception {
        final Date threeHoursLater = Hours.add(aFixedTime(), 3);

        assertThat(parseQuery("\"$addHours(now, \"3\")$\""), parsesTo(threeHoursLater));
    }

    @Test
    public void shouldBeAbleToSubtractFromDates() throws Exception {
        final Date threeHoursAgo = Hours.subtract(aFixedTime(), 3);

        assertThat(parseQuery("\"$subtractHours(now, \"3\")$\""), parsesTo(threeHoursAgo));
    }

    @Test
    public void shouldBeAbleToUsePropertyValueAsArgument() throws Exception {
        final Date threeHoursLater = Hours.add(aFixedTime(), 3);

        assertThat(parseQuery("\"$addHours(now, properties(\"some.property.value.of.three\"))$\""), parsesTo(threeHoursLater));
    }

    private Matcher<Predicate<Record>> parsesTo(Date threeHoursLater1) {
        Predicate<Record> where = Predicates.where(timeKeyword, Grammar.is(threeHoursLater1));
        return Matchers.is(where);
    }

    private Date aFixedTime() {
        final Date fixedTime = Dates.date(1983, 7, 10, 0);
        setClockTo(new FixedClock(fixedTime));
        return fixedTime;
    }

    private Predicate<Record> parseQuery(String query) {
        PredicateParser parser = application.usingRequestScope(new Callable1<Container, PredicateParser>() {
            @Override
            public PredicateParser call(Container container) throws Exception {
                return container.get(PredicateParser.class);
            }
        });

        return parser.parse(query, Sequences.one(timeKeyword));
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
