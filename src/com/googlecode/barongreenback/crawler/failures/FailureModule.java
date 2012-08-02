package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class FailureModule implements ResourcesModule, RequestScopedModule, ApplicationScopedModule {
    @Override
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(FailureResource.class));
        return this;
    }

    @Override
    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(HttpJobFailureMarshaller.class);
        container.add(PaginatedJobFailureMarshaller.class);
        container.add(MasterPaginatedJobFailureMarshaller.class);
        container.add(FailureRepository.class);
        container.add(Failures.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(Failures.class);
        return this;
    }
}
