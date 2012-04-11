package com.googlecode.barongreenback.persistence;

import com.googlecode.totallylazy.Value;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.closeable.CloseableContainer;
import com.googlecode.yadic.resolvers.Resolvers;

import java.io.Closeable;
import java.io.IOException;

public class PersistenceRequestScope implements Value<Container>, Closeable {
    private final CloseableContainer value;

    public PersistenceRequestScope(Container requestScope) throws Exception {
        PersistenceApplicationScope applicationScope = (PersistenceApplicationScope) requestScope.resolve(PersistenceApplicationScope.class);
        this.value = CloseableContainer.closeableContainer(Resolvers.listOf(requestScope, applicationScope.value()));
    }

    @Override
    public Container value() {
        return value;
    }

    @Override
    public void close() throws IOException {
//        value.close();
    }
}
