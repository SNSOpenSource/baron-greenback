package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Value;

import java.util.Properties;

public class JobHistoryItemLifespanInHours implements Value<Integer> {

    public final static Integer DEFAULT = 24;
    public final static String PROPERTY_NAME = "baron-greenback.job.history.item.lifespan.in.hours";

    private final Integer value;

    public JobHistoryItemLifespanInHours(Properties properties) {
        this(Integer.valueOf(properties.getProperty(PROPERTY_NAME, String.valueOf(DEFAULT))));
    }

    public JobHistoryItemLifespanInHours(Integer value) {
        this.value = value;
    }

    @Override
    public Integer value() {
        return value;
    }
}
