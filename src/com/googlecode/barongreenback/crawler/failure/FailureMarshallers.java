package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.crawler.StagedJob;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Predicates.where;

public enum FailureMarshallers {
    //please don't change enum names--these are stored in lucene
    http(HttpJob.class, HttpJobFailureMarshaller.class),
    paginated(PaginatedHttpJob.class, PaginatedJobFailureMarshaller.class),
    master(MasterPaginatedHttpJob.class, MasterPaginatedJobFailureMarshaller.class);

    private final Class<? extends StagedJob> jobClass;
    private final Class<? extends FailureMarshaller> marshallerClass;

    FailureMarshallers(Class<? extends StagedJob> jobClass, Class<? extends FailureMarshaller> marshallerClass) {
        this.jobClass = jobClass;
        this.marshallerClass = marshallerClass;
    }

    public FailureMarshaller marshaller(Container scope) {
        return scope.get(marshallerClass);
    }

    public static FailureMarshallers forJob(StagedJob job) {
        return Sequences.sequence(values()).find(where(jobClass(), Predicates.<Class<? extends StagedJob>>is(job.getClass()))).get();
    }

    private static Callable1<FailureMarshallers, Class<? extends StagedJob>> jobClass() {
        return new Callable1<FailureMarshallers, Class<? extends StagedJob>>() {
            @Override
            public Class<? extends StagedJob> call(FailureMarshallers failureMarshallers) throws Exception {
                return failureMarshallers.jobClass;
            }
        };
    }
}