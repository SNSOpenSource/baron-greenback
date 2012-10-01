package com.googlecode.barongreenback.queues;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class QueuesModule implements ResourcesModule, ApplicationScopedModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(QueuesResource.class));
        return this;
    }


    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(Queues.class, RequestQueues.class);
        container.add(Completer.class, CpuBoundedCompleter.class);
        return this;
    }
}
