package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ViewsModule implements ResourcesModule, RequestScopedModule, ApplicationScopedModule {
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(ViewsResource.class)).
                add(annotatedClass(BatchViewsResource.class));
    }

    public Container addPerRequestObjects(Container container) throws Exception {
        return container.add(RecordsService.class).add(ViewsRepository.class);
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        return container.add(ViewsExecutor.class);
    }
}