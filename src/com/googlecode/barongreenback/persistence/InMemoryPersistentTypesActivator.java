package com.googlecode.barongreenback.persistence;

import com.googlecode.barongreenback.jobshistory.JobId;
import com.googlecode.lazyrecords.mappings.LexicalLongMapping;
import com.googlecode.lazyrecords.mappings.LongMapping;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.UUIDMapping;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.Callable;

public class InMemoryPersistentTypesActivator implements Callable<PersistentTypes> {

    @Override
    public PersistentTypes call() throws Exception {
        return new InMemoryPersistentTypes()
                .add(String.class)
                .add(Date.class)
                .add(URI.class)
                .add(Long.class, new LongMapping())
                .add(JobId.class, jobIdMapping());
    }

    private StringMapping<JobId> jobIdMapping() {
        return new StringMapping<JobId>() {
            @Override
            public JobId toValue(String value) throws Exception {
                return new JobId(new UUIDMapping().toValue(value));
            }

            @Override
            public String toString(JobId value) throws Exception {
                return new UUIDMapping().toString(value.value());
            }
        };
    }
}
