package com.googlecode.barongreenback.views;

import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class FacetsModule implements RequestScopedModule, ResourcesModule {

    @Override
    public Container addPerRequestObjects(Container container) throws Exception {
        return container.addActivator(FacetedRecords.class, FacetedRecordsActivator.class);
    }

    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(FacetsResource.class));
    }
}
