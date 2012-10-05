package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.RecordsService;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ViewsModule implements ResourcesModule, RequestScopedModule{
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(ViewsResource.class)).
                add(annotatedClass(BatchViewsResource.class));
    }

    public Container addPerRequestObjects(Container container) throws Exception {
        return container.add(RecordsService.class).add(ViewsRepository.class);
    }
}