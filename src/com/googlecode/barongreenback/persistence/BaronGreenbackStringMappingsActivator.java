package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class BaronGreenbackStringMappingsActivator implements Callable<BaronGreenbackStringMappings> {
    private final Container requestScope;
    private final PersistentTypes persistentTypes;

    public BaronGreenbackStringMappingsActivator(BaronGreenbackRequestScope requestScope, PersistentTypes persistentTypes) {
        this.persistentTypes = persistentTypes;
        this.requestScope = requestScope.value();
    }

    @Override
    public BaronGreenbackStringMappings call() throws Exception {
        return BaronGreenbackStringMappings.baronGreenbackStringMappings(stringMappings(), persistentTypes);
    }
    private StringMappings stringMappings() {
        if (requestScope.contains(StringMappings.class)) {
            return requestScope.get(StringMappings.class);
        }

        return new StringMappings();
    }
}
