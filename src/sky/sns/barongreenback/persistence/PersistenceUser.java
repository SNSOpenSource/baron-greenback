package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

public class PersistenceUser implements Value<String> {
    public static final String PROPERTY_NAME = "persistence.user";
    private final String value;

    public PersistenceUser(String value) {
        this.value = value;
    }

    public PersistenceUser(BaronGreenbackProperties properties) {
        this(properties.getProperty(PROPERTY_NAME,  "SA"));
    }

    @Override
    public String value() {
        return value;
    }
}
