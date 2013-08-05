package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.Seconds;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompiledLessTest {
    @Test
    public void areStaleBeforeDate() throws Exception {
        Date today = Dates.date(2001, 1, 1);
        CompiledLess compiledLess = new CompiledLess("", today);
        assertThat(compiledLess.stale(today), is(false));
        assertThat(compiledLess.stale(Seconds.add(today, 1)), is(true));
        assertThat(compiledLess.stale(Seconds.subtract(today, 1)), is(false));
    }
}
