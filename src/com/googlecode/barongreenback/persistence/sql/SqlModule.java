package com.googlecode.barongreenback.persistence.sql;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecordsActivator;
import com.googlecode.barongreenback.persistence.Persistence;
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
import java.util.concurrent.Callable;

public class SqlModule implements RequestScopedModule {
    @Override
    public Module addPerRequestObjects(Container container) throws Exception {
        container.addActivator(Connection.class, ConnectionActivator.class);
        container.add(Schema.class, SqlSchema.class);
        container.add(SqlRecords.class);
        container.addActivator(Records.class, container.getActivator(SqlRecords.class));
        container.decorate(Records.class, SchemaGeneratingRecords.class);
        container.add(BaronGreenbackRecords.class);
//        container.addActivator(BaronGreenbackRecords.class, new BaronGreenbackRecordsActivator(container, SqlRecords.class, SqlSchema.class));
        container.add(SqlMappings.class);
        container.add(Persistence.class, SqlPersistence.class);
        return this;
    }
}
