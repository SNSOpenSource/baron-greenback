package com.googlecode.barongreenback.batch;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class BatchModule implements ResourcesModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(BatchResource.class));
        return this;
    }
}
