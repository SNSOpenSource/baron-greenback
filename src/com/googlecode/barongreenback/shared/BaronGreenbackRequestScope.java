package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.shared.BaronGreenbackApplicationScope;
import com.googlecode.totallylazy.Value;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.closeable.CloseableContainer;
import com.googlecode.yadic.resolvers.Resolvers;

import java.io.Closeable;
import java.io.IOException;

public class BaronGreenbackRequestScope implements Value<Container>, Closeable {
    private final CloseableContainer value;

    public BaronGreenbackRequestScope(Container requestScope) throws Exception {
        BaronGreenbackApplicationScope applicationScope = (BaronGreenbackApplicationScope) requestScope.resolve(BaronGreenbackApplicationScope.class);
        this.value = CloseableContainer.closeableContainer(Resolvers.listOf(requestScope, applicationScope.value()));
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
