package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.datasources.DataSource;
import com.googlecode.barongreenback.crawler.datasources.HttpDataSource;
import com.googlecode.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.jobs.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static java.lang.String.format;

public class HttpCrawler implements Crawler {
    private final CheckpointHandler checkpointHandler;
    private final Container requestContainer;
    private final HttpVisitedFactory visitedFactory;
    private final CrawlerRepository crawlerRepository;

    public HttpCrawler(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, Container requestContainer, HttpVisitedFactory visitedFactory) {
        this.crawlerRepository = crawlerRepository;
        this.checkpointHandler = checkpointHandler;
        this.requestContainer = requestContainer;
        this.visitedFactory = visitedFactory;
    }

    @Override
    public Number crawl(final UUID id) throws Exception {
        final Model crawler = crawlerRepository.crawlerFor(id);
        Definition source = Crawler.methods.sourceDefinition(crawler);
        Definition destination = Crawler.methods.destinationDefinition(crawler);
        assertThereIsOnlyOneUniqueField(destination);
        DataSource httpDataSource = HttpDataSource.httpDataSource(Crawler.methods.from(crawler), source);
        Container crawlerScope = CrawlerScope.crawlerScope(requestContainer, new CheckpointUpdater(checkpointHandler, id, crawler));

        MasterPaginatedHttpJob job = masterPaginatedHttpJob(id, httpDataSource, destination, checkpointHandler.lastCheckpointFor(crawler),
                Crawler.methods.more(crawler), visitedFactory, crawlerScope.get(Clock.class));
        HttpJobExecutor jobExecutor = crawlerScope.get(HttpJobExecutor.class);
        return jobExecutor.executeAndWait(job);
    }

    private Sequence<Keyword<?>> assertThereIsOnlyOneUniqueField(Definition definition) {
        Sequence<Keyword<?>> uniques = definition.fields().filter(RecordDefinition.UNIQUE_FILTER);
        if (uniques.size() != 1) {
            throw new IllegalStateException(format("There should be exactly 1 unique field, instead there are %s (%s).\nPlease correct the crawler definition.",
                    uniques.size(), uniques));
        }
        return uniques;
    }
}