package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.shared.BaronGreenbackRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.lucene.Persistence;
import com.googlecode.lazyrecords.sql.SqlRecords;
import com.googlecode.lazyrecords.sql.SqlSchema;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

import java.sql.Connection;

import static com.googlecode.yadic.Containers.addActivatorIfAbsent;
import static com.googlecode.yadic.Containers.addIfAbsent;

public class SqlModule implements RequestScopedModule {
    @Override
    public Container addPerRequestObjects(Container requestScope) throws Exception {
        final Container container = requestScope.get(BaronGreenbackRequestScope.class).value();
        addActivatorIfAbsent(container, Connection.class, ConnectionActivator.class);
        addIfAbsent(container, Persistence.class, SqlPersistence.class);
        addIfAbsent(container, SqlMappings.class);
        addIfAbsent(container, Schema.class, SqlSchema.class);
        addIfAbsent(container, SqlRecords.class);
        if (!container.contains(Records.class)) {
            container.addActivator(Records.class, container.getActivator(SqlRecords.class));
            container.decorate(Records.class, SchemaGeneratingRecords.class);
        }
        return container;
    }
}
