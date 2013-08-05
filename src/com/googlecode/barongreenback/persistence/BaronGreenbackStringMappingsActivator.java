package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class BaronGreenbackStringMappingsActivator implements Callable<BaronGreenbackStringMappings> {
    private final Container requestScope;

    public BaronGreenbackStringMappingsActivator(BaronGreenbackRequestScope requestScope) {
        this.requestScope = requestScope.value();
    }

    @Override
    public BaronGreenbackStringMappings call() throws Exception {
        return BaronGreenbackStringMappings.baronGreenbackStringMappings(requestScope.get(StringMappings.class));
    }
}
