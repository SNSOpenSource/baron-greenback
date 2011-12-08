package com.googlecode.barongreenback.jobs;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.*;
import com.googlecode.yadic.Container;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class JobsModule implements ResourcesModule, ApplicationScopedModule, RequestScopedModule, StartupModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(JobsResource.class));
        resources.add(annotatedClass(BatchJobsResource.class));
        return this;
    }


    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(HttpScheduler.class);
        container.add(Jobs.class);
        return this;
    }

    public Module addPerApplicationObjects(Container container) throws Exception {
        container.addInstance(ScheduledExecutorService.class, Executors.newScheduledThreadPool(5));
        container.add(Scheduler.class, FixedScheduler.class);
        return this;
    }

    public Container start(Container container){
        BatchJobsResource batchJobsResource = container.get(BatchJobsResource.class);
        batchJobsResource.start();
        return container;
    }
}
