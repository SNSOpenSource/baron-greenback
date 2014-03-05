package com.googlecode.barongreenback.persistence;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.Callable;

public class InMemoryTypesActivator implements Callable<PersistentTypes> {

    @Override
    public PersistentTypes call() throws Exception {
        return new InMemoryPersistentTypes().add(String.class).add(Date.class).add(URI.class);
    }
}
