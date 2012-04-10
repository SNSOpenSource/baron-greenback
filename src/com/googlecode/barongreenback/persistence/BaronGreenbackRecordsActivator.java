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

public class BaronGreenbackRecordsActivator implements Callable<BaronGreenbackRecords> {
    private final Container requestScope;

    public BaronGreenbackRecordsActivator(PersistenceRequestScope requestScope) {
        this.requestScope = requestScope.value();
    }

    @Override
    public BaronGreenbackRecords call() throws Exception {
        return BaronGreenbackRecords.records(requestScope.get(Records.class));
    }
}
