package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.lucene.Persistence;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class PersistenceActivator implements Callable<Persistence> {
    private final Container requestScope;

    public PersistenceActivator(BaronGreenbackRequestScope requestScope) {
        this.requestScope = requestScope.value();
    }

    @Override
    public Persistence call() throws Exception {
        return requestScope.get(Persistence.class);
    }
}
