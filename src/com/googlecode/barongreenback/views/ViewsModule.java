package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.search.SearchService;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ViewsModule implements ResourcesModule, RequestScopedModule{
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(ViewsResource.class));
        resources.add(annotatedClass(BatchViewsResource.class));
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(Views.class);
        container.add(SearchService.class);
        return this;
    }
}
