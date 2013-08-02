package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.parser.PredicateParser;

import java.util.concurrent.Callable;

public class PredicateBuilderActivator implements Callable<PredicateBuilder> {
    private final BaronGreenbackRequestScope requestScope;

    public PredicateBuilderActivator(BaronGreenbackRequestScope requestScope) {
        this.requestScope = requestScope;
    }

    @Override
    public PredicateBuilder call() throws Exception {
        return new PredicateBuilder(requestScope.value().get(PredicateParser.class));
    }
}
