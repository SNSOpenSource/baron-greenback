package com.googlecode.barongreenback.persistence;

import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

public class PersistenceActivator implements Callable<Persistence> {
    private final Container applicationScope;

    public PersistenceActivator(PersistenceApplicationScope applicationScope) {
        this.applicationScope = applicationScope.value();
    }

    @Override
    public Persistence call() throws Exception {
        return applicationScope.get(Persistence.class);
    }
}
