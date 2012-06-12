package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.shared.RecordDefinitionActivator;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.*;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.generics.TypeFor;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class CrawlerModule implements ResourcesModule, ArgumentScopedModule, RequestScopedModule, ApplicationScopedModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(CrawlerResource.class));
        resources.add(annotatedClass(BatchCrawlerResource.class));
        return this;
    }

    public Module addPerArgumentObjects(Container container) throws Exception {
        container.addActivator(RecordDefinition.class, RecordDefinitionActivator.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(CompositeCrawler.class);
        container.add(CheckPointHandler.class);
        container.add(Crawler.class, SequentialCrawler.class);
        container.add(CrawlInterval.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(RetryQueue.class);
        container.addType(new TypeFor<JobExecutor<InputHandler>>(){}.get(), returns(new JobExecutor<InputHandler>(Executors.newFixedThreadPool(20))));
        container.addType(new TypeFor<JobExecutor<DataMapper>>(){}.get(), returns(new JobExecutor<DataMapper>(Executors.newCachedThreadPool())));
        container.addType(new TypeFor<JobExecutor<PersistentDataWriter>>(){}.get(), returns(new JobExecutor<PersistentDataWriter>(Executors.newSingleThreadExecutor())));

        return this;
    }

    private <T> Resolver<T> returns(final T instance) {
        return new Resolver<T>() {
            @Override
            public T resolve(Type type) throws Exception {
                return instance;
            }
        };
    }

}
