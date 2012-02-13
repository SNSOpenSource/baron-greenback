package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.lucene.LuceneRecords;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

public class LuceneBaronGreenbackRecordsActivator implements Callable<BaronGreenbackRecords>, Closeable {
    private final Resolver resolver;
    private LuceneRecords records;

    public LuceneBaronGreenbackRecordsActivator(Resolver resolver) {
        this.resolver = resolver;
       
    }

    @Override
    public BaronGreenbackRecords call() throws Exception {
        records = new SimpleContainer(resolver).add(LuceneRecords.class).get(LuceneRecords.class);
        return BaronGreenbackRecords.records(records);
    }

    @Override
    public void close() throws IOException {
        Closeables.close(records);
    }
}
