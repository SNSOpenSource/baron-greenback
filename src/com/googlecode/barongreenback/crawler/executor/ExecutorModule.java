package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ExecutorModule implements ResourcesModule, ApplicationScopedModule {
    @Override
    public Resources addResources(Resources resources) throws Exception {
        return resources.add(annotatedClass(CrawlerExecutorConfigResource.class));
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        return container
                .addActivator(CrawlerExecutors.class, CrawlerExecutorsActivator.class)
                .add(ExecutorFactory.class, ThreadPoolExecutorFactory.class);
    }
}
