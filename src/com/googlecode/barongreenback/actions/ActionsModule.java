package com.googlecode.barongreenback.actions;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.annotations.HttpMethod;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;

import java.util.concurrent.Callable;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ActionsModule implements ResourcesModule, RequestScopedModule {
    private static final String ANY_ACTION_APART_FROM_LIST = "^\\/[^\\/]+\\/actions\\/(?!list).*";

    @Override
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(ActionsResource.class));
        return this;
    }

    @Override
    public Module addPerRequestObjects(final Container container) throws Exception {
        final Resolver<TemplateName> templateNameResolver = container.remove(TemplateName.class);
        container.addActivator(TemplateName.class, new Callable<TemplateName>() {
            @Override
            public TemplateName call() throws Exception {
                final Resolver<Request> requestResolver = container.getResolver(Request.class);
                Request request = requestResolver.resolve(Request.class);
                if (request.uri().path().matches(ANY_ACTION_APART_FROM_LIST)) {
                    if (request.method().equals(HttpMethod.GET)) {
                        return TemplateName.templateName("button");
                    }
                }
                return templateNameResolver.resolve(TemplateName.class);
            }
        });
        return this;
    }


}
