package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.Auditor;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;

public class QueuesCrawler extends AbstractCrawler {
    private final CrawlerHttpClient crawlerHttpHandler;
    private final PrintStream log;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final CrawlerFailures retry;
    private final StagedJobExecutor executor;

    public QueuesCrawler(final ModelRepository modelRepository, final CrawlerHttpClient crawlerHttpHandler,
                         CheckPointHandler checkpointHandler, StringMappings mappings, CrawlerFailures retry,
                         PrintStream log, StagedJobExecutor executor) {
        super(modelRepository);
        this.crawlerHttpHandler = crawlerHttpHandler;
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.retry = retry;
        this.log = log;
        this.executor = executor;
    }

    @Override
    public Number crawl(final UUID id) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);
        checkOnlyOne(destination);

        updateView(crawler, destination.fields());

        HttpDatasource datasource = HttpDatasource.dataSource(from(crawler), source);

        Container crawlContainer = crawlContainer(id, crawler);

        executor.crawl(masterPaginatedHttpJob(crawlContainer, datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));

        crawlContainer.get(CountLatch.class).await();
        return crawlContainer.get(AtomicInteger.class).get();
    }


    private Container crawlContainer(UUID id, Model crawler) {
        Container container = new SimpleContainer();
        container.addInstance(PrintStream.class, log);
        container.add(Auditor.class, PrintAuditor.class);
        container.addInstance(HttpHandler.class, crawlerHttpHandler);
        container.add(HttpClient.class, AuditHandler.class);
        container.addInstance(CrawlerFailures.class, retry);
        container.add(FailureHandler.class);
        container.add(CountLatch.class);
        container.addInstance(AtomicInteger.class, new AtomicInteger(0));
        container.addInstance(CheckpointUpdater.class, new CheckpointUpdater(checkpointHandler, id, crawler));
        return container;
    }

    private Sequence<Keyword<?>> checkOnlyOne(Definition definition) {
        Sequence<Keyword<?>> uniques = definition.fields().filter(RecordDefinition.UNIQUE_FILTER);
        if (uniques.size() != 1) {
            throw new IllegalStateException("There should be exactly 1 unique field, instead there are " + uniques.size() + " (" + uniques + ").\n" +
                    "Please correct the crawler definition.");
        }
        return uniques;
    }
}