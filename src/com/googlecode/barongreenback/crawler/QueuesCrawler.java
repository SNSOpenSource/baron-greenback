package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Container;

import java.io.PrintStream;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;

public class QueuesCrawler extends AbstractCrawler {
    private final CrawlerHttpClient crawlerHttpHandler;
    private final PrintStream log;
    private final CheckPointHandler checkpointHandler;
    private final StringMappings mappings;
    private final CrawlerFailures retry;
    private final StagedJobExecutor executor;
    private final Container requestContainer;

    public QueuesCrawler(CrawlerRepository crawlerRepository, ModelRepository modelRepository, CrawlerHttpClient crawlerHttpHandler,
                         CheckPointHandler checkpointHandler, StringMappings mappings, CrawlerFailures retry, PrintStream log,
                         StagedJobExecutor executor, Container requestContainer) {
        super(crawlerRepository, modelRepository);
        this.crawlerHttpHandler = crawlerHttpHandler;
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.retry = retry;
        this.log = log;
        this.executor = executor;
        this.requestContainer = requestContainer;
    }

    @Override
    public Number crawl(final UUID id) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);
        checkOnlyOne(destination);

        updateView(crawler, destination.fields());

        HttpDatasource datasource = HttpDatasource.datasource(from(crawler), source);

        Container crawlContainer = crawlContainer(id, crawler);

        return executor.crawlAndWait(masterPaginatedHttpJob(crawlContainer, datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));
    }


    private Container crawlContainer(UUID id, Model crawler) {
        return CrawlContainer.crawlContainer(requestContainer, log, crawlerHttpHandler, retry, new CheckpointUpdater(checkpointHandler, id, crawler));
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