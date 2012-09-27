package com.googlecode.barongreenback.persistence;

import com.googlecode.totallylazy.Value;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.closeable.CloseableContainer;

import java.io.Closeable;
import java.io.IOException;

public class PersistenceApplicationScope implements Value<Container>, Closeable {
    private final CloseableContainer value;

    public PersistenceApplicationScope(Resolver applicationScope) {
        this.value = CloseableContainer.closeableContainer(applicationScope);
        this.value.addInstance(Resolver.class, this.value);
        this.value.addInstance(Container.class, this.value);
    }

    @Override
    public Container value() {
        return value;
    }

    @Override
    public void close() throws IOException {
        value.close();
    }
}
