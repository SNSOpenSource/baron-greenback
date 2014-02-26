package com.googlecode.barongreenback.persistence;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.Callable;

public class InMemoryTypesActivator implements Callable<Types> {

    @Override
    public Types call() throws Exception {
        return new InMemoryTypes().add(String.class).add(Date.class).add(URI.class);
    }
}
