package com.googlecode.barongreenback.persistence;

import com.googlecode.totallylazy.Value;

public class PersistencePassword implements Value<String> {
    private final String value;

    public PersistencePassword(String value) {
        this.value = value;
    }

    public PersistencePassword(PersistenceProperties properties) {
        this(properties.getProperty("password",  ""));
    }

    @Override
    public String value() {
        return value;
    }
}
