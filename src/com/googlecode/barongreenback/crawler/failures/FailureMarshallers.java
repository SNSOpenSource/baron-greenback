package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.jobs.HttpJob;
import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.crawler.jobs.PaginatedHttpJob;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Predicates.where;

public enum FailureMarshallers {
    //please don't change enum names--these are used as records lookup keys
    http(HttpJob.class, HttpJobFailureMarshaller.class),
    paginated(PaginatedHttpJob.class, PaginatedJobFailureMarshaller.class),
    master(MasterPaginatedHttpJob.class, MasterPaginatedJobFailureMarshaller.class);

    private final Class<? extends Job> jobClass;
    private final Class<? extends FailureMarshaller> marshallerClass;

    FailureMarshallers(Class<? extends Job> jobClass, Class<? extends FailureMarshaller> marshallerClass) {
        this.jobClass = jobClass;
        this.marshallerClass = marshallerClass;
    }

    public FailureMarshaller marshaller(Container scope) {
        return scope.get(marshallerClass);
    }

    public static FailureMarshallers forJob(Job job) {
        return Sequences.sequence(values()).find(where(jobClass(), Predicates.<Class<? extends Job>>is(job.getClass()))).get();
    }

    private static Callable1<FailureMarshallers, Class<? extends Job>> jobClass() {
        return new Callable1<FailureMarshallers, Class<? extends Job>>() {
            @Override
            public Class<? extends Job> call(FailureMarshallers failureMarshallers) throws Exception {
                return failureMarshallers.jobClass;
            }
        };
    }
}