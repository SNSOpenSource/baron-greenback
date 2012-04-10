package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.persistence.Persistence;
import com.googlecode.barongreenback.persistence.PersistenceRequestScope;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.sql.SqlRecords;
import com.googlecode.lazyrecords.sql.SqlSchema;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

import java.sql.Connection;

public class SqlModule implements RequestScopedModule {
    @Override
    public Module addPerRequestObjects(Container requestScope) throws Exception {
        final Container container = requestScope.get(PersistenceRequestScope.class).value();
        container.addActivator(Connection.class, ConnectionActivator.class);
        container.add(Persistence.class, SqlPersistence.class);
        container.add(SqlMappings.class);
        container.add(Schema.class, SqlSchema.class);
        container.add(SqlRecords.class);
        container.addActivator(Records.class, container.getActivator(SqlRecords.class));
        container.decorate(Records.class, SchemaGeneratingRecords.class);
        return this;
    }
}
