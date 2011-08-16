package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import com.googlecode.yadic.Container;
import org.antlr.stringtemplate.StringTemplateGroup;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;

public class ModelRendererModule implements ResponseHandlersModule, RequestScopedModule{
    public Module addResponseHandlers(ResponseHandlers handlers) {
        handlers.add(where(entity(), is(instanceOf(Model.class))), renderer(ModelRenderer.class));
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.addActivator(TemplateName.class, TemplateNameActivator.class);
        container.addActivator(StringTemplateGroup.class, StringTemplateGroupActivator.class);
        return this;
    }
}
