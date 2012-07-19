package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.ViewsRepository;
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
    private final Container requestContainer;

    public QueuesCrawler(CrawlerRepository crawlerRepository, ViewsRepository viewsRepository, CrawlerHttpClient crawlerHttpHandler,
                         CheckPointHandler checkpointHandler, StringMappings mappings, PrintStream log, Container requestContainer) {
        super(crawlerRepository, viewsRepository);
        this.crawlerHttpHandler = crawlerHttpHandler;
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.log = log;
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

        Container crawlerScope = crawlerScope(id, crawler);

        return crawlerScope.get(StagedJobExecutor.class).crawlAndWait(
                masterPaginatedHttpJob(datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));
    }

    private Container crawlerScope(UUID id, Model crawler) {
        return CrawlerScope.crawlerScope(requestContainer, log, crawlerHttpHandler, new CheckpointUpdater(checkpointHandler, id, crawler));
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