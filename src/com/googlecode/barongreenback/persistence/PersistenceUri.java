package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Uri;

import java.util.Properties;

public class PersistenceUri extends Uri {
    public static final String PROPERTY_NAME = "persistence.uri";

    public PersistenceUri(CharSequence value) {
        super(value);
    }

    public PersistenceUri(BaronGreenbackProperties properties) {
        super(properties.getProperty(PROPERTY_NAME, "lucene:mem"));
    }

    public static String name() {
        return String.format("%s.%s", BaronGreenbackProperties.PREFIX, PROPERTY_NAME);
    }

    public static String set(Properties properties, String value) {
        return (String) properties.setProperty(name(), value);
    }
}
