package com.googlecode.barongreenback.crawler;

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

import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class CrawlerFailureRepositoryTest {
    private static final Record record = Record.constructors.record().set(Keywords.keyword("title", String.class), "Man eats dog");
    private static final Container scope = testScope();
    private static final UUID crawlerId = UUID.randomUUID();

    private Definition source;
    private Definition destination;

    @Before
    public void addCrawlerToRepo() {
        CrawlerRepository crawlerRepository = scope.get(CrawlerRepository.class);
        crawlerRepository.importCrawler(some(crawlerId), CrawlerTests.contentOf("crawler.json"));
        Model crawler = crawlerRepository.crawlerFor(crawlerId);
        source = AbstractCrawler.sourceDefinition(crawler);
        destination = AbstractCrawler.destinationDefinition(crawler);
    }

    @Test
    public void canSaveAndLoadAnHttpJobFailure() throws Exception {
        UUID crawlerFailureId = UUID.randomUUID();

        Failure failure = Failure.failure(HttpJob.job(HttpDatasource.datasource(uri("/any/uri"), crawlerId, source, record), destination), "Bigtime failure");

        CrawlerFailureRepository repository = scope.get(CrawlerFailureRepository.class);
        repository.set(crawlerFailureId, failure);
        assertThat(repository.get(crawlerFailureId), is(some(failure)));
    }

    private static Container testScope() {
        Container scope = new SimpleContainer();
        scope.add(CrawlerFailureRepository.class);
        scope.add(BaronGreenbackRecords.class);
        scope.add(Records.class, MemoryRecords.class);
        scope.addInstance(StringMappings.class, new StringMappings().add(Model.class, new ModelMapping()));
        scope.add(CrawlerRepository.class);
        scope.add(ModelRepository.class, RecordsModelRepository.class);
        Containers.selfRegister(scope);
        return scope;
    }
}
