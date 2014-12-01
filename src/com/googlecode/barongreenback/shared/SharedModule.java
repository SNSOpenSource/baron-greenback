package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.ArgumentScopedModule;
import com.googlecode.utterlyidle.modules.ModuleDefiner;
import com.googlecode.utterlyidle.modules.ModuleDefinitions;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.utterlyidle.rendering.ObjectRenderer;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import com.googlecode.yadic.Container;
import org.antlr.stringtemplate.StringTemplateGroup;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;
import static com.googlecode.utterlyidle.sitemesh.ContentTypePredicate.contentType;
import static com.googlecode.yadic.Containers.addIfAbsent;

public class SharedModule implements ApplicationScopedModule, ResponseHandlersModule, RequestScopedModule, ResourcesModule, ArgumentScopedModule, ModuleDefiner {
    @Override
    public ResponseHandlers addResponseHandlers(ResponseHandlers handlers) {
        return handlers.add(and(where(entity(), is(instanceOf(Model.class))), contentType(MediaType.APPLICATION_JSON)), renderer(ObjectRenderer.class)).
            add(where(entity(), is(instanceOf(Model.class))), renderer(ModelRenderer.class));
    }

    @Override
    public Container addPerRequestObjects(Container container) throws Exception {
        addIfAbsent(container, BaronGreenbackRequestScope.class);
        return container.
                addActivator(TemplateName.class, TemplateNameActivator.class).
                addActivator(RenderableTypes.class, InMemoryRenderableTypesActivator.class).
                addActivator(StringTemplateGroup.class, StringTemplateGroupActivator.class).
                addActivator(ModelRepository.class, ModelRepositoryActivator.class).
                addActivator(AdvancedMode.class, AdvancedModeActivator.class);
    }

    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(HomeResource.class));
    }

    @Override
    public Container addPerArgumentObjects(Container container) throws Exception {
        return container.addActivator(Model.class, ModelActivator.class);
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        addIfAbsent(container, BaronGreenbackApplicationScope.class);
        return container.add(BaronGreenbackProperties.class).add(ModelCache.class);
    }

    @Override
    public ModuleDefinitions defineModules(ModuleDefinitions moduleDefinitions) throws Exception {
        return moduleDefinitions.addApplicationModule(BaronGreenbackApplicationScopedModule.class).
                addRequestModule(BaronGreenbackRequestScopedModule.class);
    }


}