package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.shared.RecordDefinitionActivator;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.*;
import com.googlecode.yadic.Container;

import java.util.concurrent.*;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class CrawlerModule implements ResourcesModule, ArgumentScopedModule, RequestScopedModule, ApplicationScopedModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(CrawlerResource.class));
        resources.add(annotatedClass(BatchCrawlerResource.class));
        resources.add(annotatedClass(CrawlerStatusResource.class));
        return this;
    }

    public Module addPerArgumentObjects(Container container) throws Exception {
        container.addActivator(RecordDefinition.class, RecordDefinitionActivator.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(CompositeCrawler.class);
        container.add(CheckPointHandler.class);
        container.add(Crawler.class, QueuesCrawler.class);
        container.add(CrawlInterval.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(RetryQueue.class);
        container.addInstance(InputHandler.class, new InputHandler(executor(20, 100)));
        container.addInstance(DataMapper.class, new DataMapper(executor(100, 100)));
        container.addInstance(PersistentDataWriter.class, new PersistentDataWriter(executor(1, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy())));
        return this;
    }

    private ThreadPoolExecutor executor(int threads, int capacity) {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(capacity);
        return executor(threads, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private ThreadPoolExecutor executor(int threads, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler policy) {
        return new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                workQueue, policy);
    }
}
