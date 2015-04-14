package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.PrefixProperties;

import java.util.Properties;

public class BaronGreenbackProperties extends PrefixProperties {
    public static final String PREFIX = "baron-greenback";

    public BaronGreenbackProperties() {
        super(PREFIX);
    }

    public BaronGreenbackProperties(Properties parent) {
        super(PREFIX, parent);
    }
}
