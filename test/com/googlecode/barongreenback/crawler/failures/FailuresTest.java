package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.AbstractCrawler;
import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.CrawlerTests;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.HttpDatasource.httpDatasource;
import static com.googlecode.barongreenback.crawler.HttpJob.httpJob;
import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static com.googlecode.barongreenback.crawler.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.barongreenback.crawler.VisitedFactory.visitedFactory;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class FailuresTest {
    private static final Record record = Record.constructors.record().set(Keywords.keyword("title", String.class), "Man eats dog");
    private static final Container scope = testScope();

    private static final UUID crawlerId = UUID.randomUUID();
    private Definition source;
    private Definition destination;

    @Before
    public void addCrawlerToRepo() {
        CrawlerRepository crawlerRepository = scope.get(CrawlerRepository.class);
        crawlerRepository.importCrawler(some(crawlerId), CrawlerTests.contentOf("crawlerForFailures.json"));
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        source = AbstractCrawler.sourceDefinition(crawler);
        destination = AbstractCrawler.destinationDefinition(crawler);
    }

    @Test
    public void canSaveAndLoadAnHttpJobFailure() throws Exception {
        assertCanPersistAndLoad(Failure.failure(httpJob(crawlerId, record, httpDatasource(uri("/any/uri"), source), destination, visitedFactory().value()), "Bigtime failures"));
    }

    @Test
    public void canSaveAndLoadAPaginatedHttpJobFailure() throws Exception {
        assertCanPersistAndLoad(Failure.failure(paginatedHttpJob(crawlerId, record, httpDatasource(uri("/any/uri"), source), destination, "checkpoint", "/some/xpath", scope.get(StringMappings.class), visitedFactory().value()), "Bigtime failures"));
    }

    @Test
    public void canSaveAndLoadAMasterPaginatedHttpJobFailure() throws Exception {
        assertCanPersistAndLoad(Failure.failure(masterPaginatedHttpJob(crawlerId, httpDatasource(uri("/any/uri"), source), destination, "checkpoint", "/some/xpath", scope.get(StringMappings.class), visitedFactory()), "Bigtime failures"));
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
        scope.add(Records.class, MemoryRecords.class);
        scope.addInstance(StringMappings.class, new StringMappings().add(Model.class, new ModelMapping()));
        scope.add(CrawlerRepository.class);
        scope.add(ModelRepository.class, RecordsModelRepository.class);
        scope.add(CheckpointHandler.class);
        scope.add(HttpJobFailureMarshaller.class);
        scope.add(PaginatedJobFailureMarshaller.class);
        scope.add(MasterPaginatedJobFailureMarshaller.class);
        scope.add(Failures.class);
        scope.add(VisitedFactory.class);
        Containers.selfRegister(scope);
        return scope;
    }
}
