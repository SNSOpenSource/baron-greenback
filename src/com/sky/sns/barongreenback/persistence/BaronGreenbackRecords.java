package com.sky.sns.barongreenback.persistence;

import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Value;

public class BaronGreenbackRecords implements Value<Records> {
    private final Records records;

    private BaronGreenbackRecords(Records records) {
        this.records = records;
    }

    public static BaronGreenbackRecords records(Records records) {
        return new BaronGreenbackRecords(records);
    }

    @Override
    public Records value() {
        return records;
    }
}
