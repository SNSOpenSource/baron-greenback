package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Value;

public class BaronGreenbackStringMappings implements Value<StringMappings> {
    private final StringMappings mappings;

    private BaronGreenbackStringMappings(StringMappings mappings) {
        this.mappings = mappings;
    }

    public static BaronGreenbackStringMappings baronGreenbackStringMappings(StringMappings mappings) {
        return new BaronGreenbackStringMappings(mappings);
    }

    @Override
    public StringMappings value() {
        return mappings;
    }
}
