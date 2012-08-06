package com.googlecode.barongreenback.jobs;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.StartupModule;
import com.googlecode.yadic.Container;

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
        container.add(Scheduler.class, FixedScheduler.class);
        return this;
    }

    public Container start(Container container){
        BatchJobsResource batchJobsResource = container.get(BatchJobsResource.class);
        batchJobsResource.start();
        return container;
    }
}
