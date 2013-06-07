package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

public class PersistencePassword implements Value<String> {
    public static final String PROPERTY_NAME = "persistence.password";
    private final String value;

    public PersistencePassword(String value) {
        this.value = value;
    }

    public PersistencePassword(BaronGreenbackProperties properties) {
        this(properties.getProperty(PROPERTY_NAME,  ""));
    }

    @Override
    public String value() {
        return value;
    }
}
