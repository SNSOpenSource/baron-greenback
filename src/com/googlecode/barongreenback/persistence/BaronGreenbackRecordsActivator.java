package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.barongreenback.persistence.BaronGreenbackRecords.records;

public class BaronGreenbackRecordsActivator implements Callable<BaronGreenbackRecords>, Closeable {
    private final Resolver resolver;
    private final Class<? extends Records> recordClass;
    private final Class<? extends Schema> schemaClass;
    private Records records;

    public BaronGreenbackRecordsActivator(Resolver resolver, Class<? extends Records> recordsClass, Class<? extends Schema> schemaClass) {
        this.resolver = resolver;
        this.recordClass = recordsClass;
        this.schemaClass = schemaClass;
    }

    @Override
    public BaronGreenbackRecords call() throws Exception {
        Container container = new SimpleContainer(resolver);
        return records(records = container.
                add(schemaClass).
                addActivator(Schema.class, container.getActivator(schemaClass)).
                add(recordClass).
                addActivator(Records.class, container.getActivator(recordClass)).
                decorate(Records.class, SchemaGeneratingRecords.class).
                get(Records.class));
    }

    @Override
    public void close() throws IOException {
        Closeables.close(records);
    }
}
