package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class KeepBackups implements Value<Integer> {
    public static final String PROPERTY_NAME = "backups.keep";
    public static final int DEFAULT = 10;
    private final int value;

    public KeepBackups(BaronGreenbackProperties properties) {
        this.value = parseInt(properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT)));
    }

    @Override
    public Integer value() {
        return value;
    }
}
