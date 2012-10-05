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
    public Resources addResources(Resources resources) throws Exception {
        return resources.
                add(annotatedClass(JobsResource.class)).
                add(annotatedClass(BatchJobsResource.class));
    }


    public Container addPerRequestObjects(Container container) throws Exception {
        return container.
                add(HttpScheduler.class).
                add(Jobs.class);
    }

    public Container addPerApplicationObjects(Container container) throws Exception {
        return container.add(Scheduler.class, FixedScheduler.class);
    }

    public Container start(Container container){
        container.get(BatchJobsResource.class).start();
        return container;
    }
}
