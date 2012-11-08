package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.ArgumentScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import com.googlecode.yadic.Container;
import org.antlr.stringtemplate.StringTemplateGroup;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;

public class SharedModule implements ApplicationScopedModule, ResponseHandlersModule, RequestScopedModule, ResourcesModule, ArgumentScopedModule {
    public ResponseHandlers addResponseHandlers(ResponseHandlers handlers) {
        return handlers.add(where(entity(), is(instanceOf(Model.class))), renderer(ModelRenderer.class));
    }

    public Container addPerRequestObjects(Container container) throws Exception {
        return container.
                addActivator(TemplateName.class, TemplateNameActivator.class).
                addActivator(StringTemplateGroup.class, StringTemplateGroupActivator.class).
                add(ModelRepository.class, RecordsModelRepository.class).
                decorate(ModelRepository.class, CachingModelRepository.class).
                addActivator(AdvancedMode.class, AdvancedModeActivator.class).
                add(InvocationHandler.class, InternalInvocationHandler.class);
    }

    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(HomeResource.class));
    }

    public Container addPerArgumentObjects(Container container) throws Exception {
        return container.addActivator(Model.class, ModelActivator.class);
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        return container.add(BaronGreenbackProperties.class).
                add(ModelCache.class);
    }
}