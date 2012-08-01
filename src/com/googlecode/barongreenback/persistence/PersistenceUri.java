package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Uri;

import java.util.Properties;

public class PersistenceUri extends Uri {
    public static final String NAME = "uri";

    public PersistenceUri(CharSequence value) {
        super(value);
    }

    public PersistenceUri(PersistenceProperties properties) {
        super(properties.getProperty(NAME, "lucene:mem"));
    }

    public static String name() {
        return String.format("%s.%s.%s", BaronGreenbackProperties.PREFIX, PersistenceProperties.PREFIX, NAME);
    }

    public static String set(Properties properties, String value) {
        return (String) properties.setProperty(name(), value);
    }
}
