package com.googlecode.barongreenback.actions;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ActionsModule implements ResourcesModule {
    @Override
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(ActionsResource.class));
        return this;
    }
}
