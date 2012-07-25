package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failure.CrawlerFailureRepository;
import com.googlecode.barongreenback.crawler.failure.HttpJobFailureMarshaller;
import com.googlecode.barongreenback.crawler.failure.MasterPaginatedJobFailureMarshaller;
import com.googlecode.barongreenback.crawler.failure.PaginatedJobFailureMarshaller;
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
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;
import com.googlecode.yadic.SimpleContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.HttpJob.httpJob;
import static com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob.masterPaginatedHttpJob;
import static com.googlecode.barongreenback.crawler.PaginatedHttpJob.paginatedHttpJob;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class CrawlerFailureTest {
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
        assertCanPersistAndLoad(Failure.failure(httpJob(HttpDatasource.datasource(uri("/any/uri"), crawlerId, source, record), destination), "Bigtime failure"));
    }

    @Test
    public void canSaveAndLoadAPaginatedHttpJobFailure() throws Exception {
        assertCanPersistAndLoad(Failure.failure(paginatedHttpJob(HttpDatasource.datasource(uri("/any/uri"), crawlerId, source, record), destination, "checkpoint", "/some/xpath", scope.get(StringMappings.class)), "Bigtime failure"));
    }

    @Test
    public void canSaveAndLoadAMasterPaginatedHttpJobFailure() throws Exception {
        assertCanPersistAndLoad(Failure.failure(masterPaginatedHttpJob(HttpDatasource.datasource(uri("/any/uri"), crawlerId, source, record), destination, "checkpoint", "/some/xpath", scope.get(StringMappings.class)), "Bigtime failure"));
    }

    private void assertCanPersistAndLoad(Failure failure) {
        CrawlerFailures failures = scope.get(CrawlerFailures.class);
        UUID crawlerFailureId = failures.add(failure);
        assertThat(failures.get(crawlerFailureId), is(some(failure)));
    }

    public static Container testScope() {
        Container scope = new SimpleContainer();
        scope.add(CrawlerFailureRepository.class);
        scope.add(BaronGreenbackRecords.class);
        scope.add(Records.class, MemoryRecords.class);
        scope.addInstance(StringMappings.class, new StringMappings().add(Model.class, new ModelMapping()));
        scope.add(CrawlerRepository.class);
        scope.add(ModelRepository.class, RecordsModelRepository.class);
        scope.add(CheckpointHandler.class);
        scope.add(HttpJobFailureMarshaller.class);
        scope.add(PaginatedJobFailureMarshaller.class);
        scope.add(MasterPaginatedJobFailureMarshaller.class);
        scope.add(CrawlerFailures.class);
        Containers.selfRegister(scope);
        return scope;
    }
}
