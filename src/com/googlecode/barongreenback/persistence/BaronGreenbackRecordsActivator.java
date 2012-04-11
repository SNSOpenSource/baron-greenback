package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.Records;
import com.googlecode.yadic.Container;

import java.util.concurrent.Callable;

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
