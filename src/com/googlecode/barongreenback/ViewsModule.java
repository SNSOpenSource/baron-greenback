package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.annotations.AnnotatedBindings;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;

import static com.googlecode.barongreenback.SearchModule.file;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;

public class ViewsModule implements ResourcesModule, RequestScopedModule, ResponseHandlersModule{
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(ViewsResource.class));
        return this;
    }

    public Module addResponseHandlers(ResponseHandlers handlers) throws Exception {
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(Views.class);
        return this;
    }
}
