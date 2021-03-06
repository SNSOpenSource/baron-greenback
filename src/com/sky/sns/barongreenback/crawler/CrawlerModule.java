package com.sky.sns.barongreenback.crawler;

import com.sky.sns.barongreenback.shared.RecordDefinition;
import com.sky.sns.barongreenback.shared.RecordDefinitionActivator;
import com.googlecode.totallylazy.StringPrintStream;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.ArgumentScopedModule;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;

import java.io.PrintStream;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class CrawlerModule implements ResourcesModule, ArgumentScopedModule, RequestScopedModule {
    public Resources addResources(Resources resources) throws Exception {
        return resources.
                add(annotatedClass(CrawlerDefinitionResource.class)).
                add(annotatedClass(BatchCrawlerResource.class)).
                add(annotatedClass(CrawlerStatusResource.class));
    }

    public Container addPerArgumentObjects(Container container) throws Exception {
        return container.addActivator(RecordDefinition.class, RecordDefinitionActivator.class);
    }

    public Container addPerRequestObjects(Container container) throws Exception {
        return container.
                add(HttpVisitedFactory.class).
                addActivator(CrawlerHttpClient.class, CrawlerHttpClientActivator.class).
                add(CompositeCrawler.class).
                add(CheckpointHandler.class).
                add(CrawlerRepository.class).
                add(Crawler.class, HttpCrawler.class).
                add(CrawlInterval.class).
                addInstance(PrintStream.class, new StringPrintStream()).
                add(CrawlerConnectTimeout.class).
                add(CrawlerReadTimeout.class);
    }
}
