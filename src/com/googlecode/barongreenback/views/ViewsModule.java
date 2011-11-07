package com.googlecode.barongreenback.views;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;

public class ViewsModule implements ResourcesModule, RequestScopedModule{
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(ViewsResource.class));
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(Views.class);
        return this;
    }
}
