package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.ApplicationTests;
import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.parser.ParserParameters;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.StoppedClock;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParserParametersTest extends ApplicationTests {

    @Test
    public void shouldProvideNowAsDate() {
        Clock firstNowClock = new StoppedClock(Dates.date(1983, 10, 7));
        setClockTo(firstNowClock);

        Date firstNow = getDateFromParserParameters();
        assertThat(firstNow, is(firstNowClock.now()));

        Clock secondNowClock = new StoppedClock(Dates.date(1970, 2, 14));
        setClockTo(secondNowClock);

        Date secondNow = getDateFromParserParameters();
        assertThat(secondNow, is(secondNowClock.now()));
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

    private Date getDateFromParserParameters() {
        return application.usingRequestScope(new Callable1<Container, Date>() {
            @Override
            public Date call(Container container) throws Exception {
                ParserParameters parserParameters = container.get(BaronGreenbackRequestScope.class).value().get(ParserParameters.class);
                return (Date) parserParameters.values().get("now");
            }
        });
    }

}
