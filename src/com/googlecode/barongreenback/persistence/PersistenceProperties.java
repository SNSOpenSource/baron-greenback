package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.PrefixProperties;

public class PersistenceProperties extends PrefixProperties {
    private static final String PREFIX = "persistence";

    public PersistenceProperties() {
        super(PREFIX);
    }

    public PersistenceProperties(BaronGreenbackProperties parent) {
        super(PREFIX, parent);
    }
}
