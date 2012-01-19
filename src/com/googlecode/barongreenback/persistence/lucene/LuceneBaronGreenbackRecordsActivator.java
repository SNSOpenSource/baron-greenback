package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.lucene.LuceneRecords;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.SimpleContainer;

import java.util.concurrent.Callable;

public class LuceneBaronGreenbackRecordsActivator implements Callable<BaronGreenbackRecords> {
    private final Resolver resolver;

    public LuceneBaronGreenbackRecordsActivator(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public BaronGreenbackRecords call() throws Exception {
        LuceneRecords luceneRecords = new SimpleContainer(resolver).add(LuceneRecords.class).get(LuceneRecords.class);
        return BaronGreenbackRecords.records(luceneRecords);
    }
}
