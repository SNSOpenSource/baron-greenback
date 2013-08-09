package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class FailureModule implements ResourcesModule, RequestScopedModule, ApplicationScopedModule {
    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(FailureResource.class));
    }

    @Override
    public Container addPerRequestObjects(Container container) throws Exception {
        return container.add(HttpJobFailureMarshaller.class).
                add(PaginatedJobFailureMarshaller.class).
                add(MasterPaginatedJobFailureMarshaller.class).
                add(FailureRepository.class).
                add(Failures.class);
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        return container.add(Failures.class);
    }
}
