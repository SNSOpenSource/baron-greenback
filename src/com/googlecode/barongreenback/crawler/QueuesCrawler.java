package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.ViewsRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;

public class QueuesCrawler extends AbstractCrawler {
    private final CheckpointHandler checkpointHandler;
    private final StringMappings mappings;
    private final Container requestContainer;

    public QueuesCrawler(CrawlerRepository crawlerRepository, ViewsRepository viewsRepository,
                         CheckpointHandler checkpointHandler, StringMappings mappings, Container requestContainer) {
        super(crawlerRepository, viewsRepository);
        this.checkpointHandler = checkpointHandler;
        this.mappings = mappings;
        this.requestContainer = requestContainer;
    }

    @Override
    public Number crawl(final UUID id) throws Exception {
        final Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);
        checkOnlyOne(destination);

        HttpDatasource datasource = HttpDatasource.datasource(from(crawler), source, Record.constructors.record());

        Container crawlerScope = crawlerScope(id, crawler);

        return crawlerScope.get(StagedJobExecutor.class).crawlAndWait(
                masterPaginatedHttpJob(id, datasource, destination, checkpointHandler.lastCheckPointFor(crawler), more(crawler), mappings));
    }

    private Container crawlerScope(UUID id, Model crawler) {
        return CrawlerScope.crawlerScope(requestContainer, new CheckpointUpdater(checkpointHandler, id, crawler));
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