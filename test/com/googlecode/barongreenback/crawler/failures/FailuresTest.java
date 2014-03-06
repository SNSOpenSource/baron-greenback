package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.Crawler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.crawler.HttpVisitedFactory;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.persistence.InMemoryPersistentTypesActivator;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.barongreenback.persistence.PersistentTypes;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.StoppedClock;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.datasources.HttpDataSource.httpDataSource;
import static com.googlecode.barongreenback.crawler.jobs.HttpJob.httpJob;
import static com.googlecode.barongreenback.crawler.jobs.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static com.googlecode.barongreenback.crawler.jobs.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.barongreenback.crawler.HttpVisitedFactory.visitedFactory;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.junit.Assert.assertThat;

public class FailuresTest {
    private static final Record record = Record.constructors.record().set(keyword("title", String.class), "Man eats dog");
    private static final Container scope = testScope();

    private static final UUID crawlerId = UUID.randomUUID();
    private Definition source;
    private Definition destination;

    @Before
    public void addCrawlerToRepo() {
        CrawlerRepository crawlerRepository = scope.get(CrawlerRepository.class);
        crawlerRepository.importCrawler(some(crawlerId), CrawlerTests.contentOf("crawlerForFailures.json"));
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        source = Crawler.methods.sourceDefinition(crawler);
        destination = Crawler.methods.destinationDefinition(crawler);
    }

    @Test
    public void canSaveAndLoadAnHttpJobFailure() throws Exception {
        Clock clock = scope.get(Clock.class);
        assertCanPersistAndLoad(Failure.failure(httpJob(crawlerId, record, httpDataSource(uri("/any/uri"), source), destination, visitedFactory().value(), clock.now()), "Bigtime failures", 0L));
    }

    @Test
    public void canSaveAndLoadAPaginatedHttpJobFailure() throws Exception {
        Clock clock = scope.get(Clock.class);
        assertCanPersistAndLoad(Failure.failure(paginatedHttpJob(crawlerId, record, httpDataSource(uri("/any/uri"), source), destination, "checkpoint", "/some/xpath", visitedFactory().value(), clock.now()), "Bigtime failures", 0L));
    }

    @Test
    public void canSaveAndLoadAMasterPaginatedHttpJobFailure() throws Exception {
        Clock clock = scope.get(Clock.class);
        assertCanPersistAndLoad(Failure.failure(masterPaginatedHttpJob(crawlerId, httpDataSource(uri("/any/uri"), source), destination, "checkpoint", "/some/xpath", visitedFactory(), clock), "Bigtime failures", 0L));
    }

    private void assertCanPersistAndLoad(Failure failure) {
        Failures failures = scope.get(Failures.class);
        UUID failureId = failures.add(failure);

        Option<Failure> right = failures.get(failureId);
        Option<Failure> left = some(failure);

        assertThat(left, is(right));
    }

    public static Container testScope() {
        Container scope = new SimpleContainer();
        scope.add(FailureRepository.class);
        scope.add(BaronGreenbackRecords.class);
        StringMappings mappings = new StringMappings().add(Model.class, new ModelMapping());
        scope.addInstance(StringMappings.class, mappings);
        scope.addInstance(Records.class, new MemoryRecords(mappings));
        scope.addActivator(PersistentTypes.class, InMemoryPersistentTypesActivator.class);
        scope.add(BaronGreenbackStringMappings.class);
        scope.add(CrawlerRepository.class);
        scope.add(ModelRepository.class, RecordsModelRepository.class);
        scope.add(CheckpointHandler.class);
        scope.add(HttpJobFailureMarshaller.class);
        scope.add(PaginatedJobFailureMarshaller.class);
        scope.add(MasterPaginatedJobFailureMarshaller.class);
        scope.add(Failures.class);
        scope.addInstance(Clock.class, new StoppedClock(date(2001, 1, 1)));
        scope.add(HttpVisitedFactory.class);
        Containers.selfRegister(scope);
        return scope;
    }
}
