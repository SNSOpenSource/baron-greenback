package com.sky.sns.barongreenback.batch;

import com.sky.sns.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class BackupInterval implements Value<Integer> {
    public static final String PROPERTY_NAME = "backup.interval";
    public static final int DEFAULT = 86400;
    private final int value;

    public BackupInterval(BaronGreenbackProperties properties) {
        this.value = parseInt(properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT)));
    }

    @Override
    public Integer value() {
        return value;
    }
}