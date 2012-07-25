package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failure.CrawlerFailureRepository;
import com.googlecode.barongreenback.crawler.failure.HttpJobFailureMarshaller;
import com.googlecode.barongreenback.crawler.failure.MasterPaginatedJobFailureMarshaller;
import com.googlecode.barongreenback.crawler.failure.PaginatedJobFailureMarshaller;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.shared.RecordDefinitionActivator;
import com.googlecode.totallylazy.StringPrintStream;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.*;
import com.googlecode.yadic.Container;
import java.io.PrintStream;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class CrawlerModule implements ResourcesModule, ArgumentScopedModule, RequestScopedModule, ApplicationScopedModule {
    public Module addResources(Resources resources) throws Exception {
        resources.add(annotatedClass(CrawlerDefinitionResource.class));
        resources.add(annotatedClass(CrawlerImplementationResource.class));
        resources.add(annotatedClass(BatchCrawlerResource.class));
        resources.add(annotatedClass(CrawlerStatusResource.class));
        resources.add(annotatedClass(CrawlerFailureResource.class));
        return this;
    }

    public Module addPerArgumentObjects(Container container) throws Exception {
        container.addActivator(RecordDefinition.class, RecordDefinitionActivator.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(CrawlerHttpClient.class);
        container.add(CompositeCrawler.class);
        container.add(CheckpointHandler.class);
        container.add(CrawlerRepository.class);
        container.add(CrawlerActivator.class);
        container.addActivator(Crawler.class, container.get(CrawlerActivator.class));
        container.add(CrawlInterval.class);
        container.addInstance(PrintStream.class, new StringPrintStream());
        container.add(HttpJobFailureMarshaller.class);
        container.add(PaginatedJobFailureMarshaller.class);
        container.add(MasterPaginatedJobFailureMarshaller.class);
        container.add(CrawlerFailureRepository.class);
        container.add(CrawlerFailures.class);
        return this;
    }

    @Override
    public Module addPerApplicationObjects(Container container) throws   Exception {
        container.add(CrawlerFailures.class);
        return this;
    }
}