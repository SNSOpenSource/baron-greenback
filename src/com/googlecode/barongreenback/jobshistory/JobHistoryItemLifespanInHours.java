package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Value;

public class JobHistoryItemLifespanInHours implements Value<Integer> {

    public final static Integer DEFAULT = 720; // 30 days

    private final Integer value;

    public JobHistoryItemLifespanInHours(){
        this(DEFAULT);
    }

    public JobHistoryItemLifespanInHours(Integer value) {
        this.value = value;
    }

    @Override
    public Integer value() {
        return value;
    }
}
