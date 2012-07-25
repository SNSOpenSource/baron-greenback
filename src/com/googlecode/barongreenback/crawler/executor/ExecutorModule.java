package com.googlecode.barongreenback.crawler.executor;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class ExecutorModule implements ResourcesModule{
    @Override
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(CrawlerExecutorConfigResource.class));
        return this;
    }
}
