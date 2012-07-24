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
    http(HttpJob.class) {
        @Override
        public FailureMarshaller marshaller(Container scope) {
            return scope.get(HttpJobFailureMarshaller.class);
        }
    },
    paginated(PaginatedHttpJob.class) {
        @Override
        public FailureMarshaller marshaller(Container scope) {
            return scope.get(PaginatedJobFailureMarshaller.class);
        }
    },
    master(MasterPaginatedHttpJob.class) {
        @Override
        public FailureMarshaller marshaller(Container scope) {
            return scope.get(MasterPaginatedJobFailureMarshaller.class);
        }
    };

    private final Class<? extends StagedJob> jobClass;

    FailureMarshallers(Class<? extends StagedJob> jobClass) {
        this.jobClass = jobClass;
    }

    abstract public FailureMarshaller marshaller(Container scope);

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
