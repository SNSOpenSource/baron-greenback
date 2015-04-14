package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import static java.lang.String.valueOf;

public class BackupStart implements Value<String> {
    public static final String PROPERTY_NAME = "backup.start";
    public static final String DEFAULT = "0400";
    private final String value;

    public BackupStart(BaronGreenbackProperties properties) {
        this.value = properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT));
    }

    @Override
    public String value() {
        return value;
    }
}
