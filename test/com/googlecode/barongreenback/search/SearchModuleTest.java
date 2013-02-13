package com.googlecode.barongreenback.search;


import org.junit.Test;

import java.util.Date;

import static com.googlecode.barongreenback.search.SearchModule.addHoursFunction;
import static com.googlecode.barongreenback.search.SearchModule.getInputDateFormat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchModuleTest {

    @Test
    public void shouldSubtractHoursFromDate() throws Exception {
        Date now = new Date();
        Date nowMinus3Hours = new Date(now.getTime() - (3 * 3600 * 1000));
        String tMinus3Hours = addHoursFunction.call(toDateWithMsFormat(now), "-3");
        assertThat(tMinus3Hours, is(toDateWithMsFormat(nowMinus3Hours)));
    }

    private String toDateWithMsFormat(Date now) {
        return getInputDateFormat().format(now);
    }
}
