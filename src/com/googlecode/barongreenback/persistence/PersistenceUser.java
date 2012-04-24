package com.googlecode.barongreenback.persistence;

import com.googlecode.totallylazy.Value;

public class PersistenceUser implements Value<String> {
    private final String value;

    public PersistenceUser(String value) {
        this.value = value;
    }

    public PersistenceUser(PersistenceProperties properties) {
        this(properties.getProperty("user",  "SA"));
    }

    @Override
    public String value() {
        return value;
    }
}
