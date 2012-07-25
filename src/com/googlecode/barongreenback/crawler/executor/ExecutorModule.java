package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ExecutorModule implements ResourcesModule, ApplicationScopedModule {
    @Override
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(CrawlerExecutorConfigResource.class));
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(CrawlerExecutorsActivator.class);
        container.addActivator(CrawlerExecutors.class, container.get(CrawlerExecutorsActivator.class));
        return this;
    }
}
