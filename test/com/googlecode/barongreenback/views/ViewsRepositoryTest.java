package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.barongreenback.shared.RecordsModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.funclate.Model.mutable.model;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ViewsRepositoryTest {
    private ViewsRepository viewsRepository;
    private RecordsModelRepository modelRepository;

    @Test
    public void ensureViewExistDoesNotCreateIfViewWithSameUpdatedNameAlreadyExists() throws Exception {
        viewsRepository.ensureViewForCrawlerExists(crawlerWithName("firstName"), Sequences.<Keyword<?>>empty());
        viewsRepository.ensureViewForCrawlerExists(crawlerWithName("secondName"), Sequences.<Keyword<?>>empty());
        assertThat(modelRepository.find(Predicates.all()).size(), is(1));
    }

    @Test
    public void ensureViewExistDoesNotCreateIfViewAlreadyExists() throws Exception {
        Model crawler = crawlerWithName("crawlerName");
        viewsRepository.ensureViewForCrawlerExists(crawler, Sequences.<Keyword<?>>empty());
        viewsRepository.ensureViewForCrawlerExists(crawler, Sequences.<Keyword<?>>empty());
        assertThat(modelRepository.find(Predicates.all()).size(), is(1));
    }

    @Test
    public void ensureViewExistCreatesWithName() throws Exception {
        String crawlerName = "crawlerName";
        Model crawler = crawlerWithName(crawlerName);
        viewsRepository.ensureViewForCrawlerExists(crawler, Sequences.<Keyword<?>>empty());
        assertThat(viewsRepository.viewForName(crawlerName), is(notNullValue()));
    }

    private Model crawlerWithName(String crawlerName) {
        return model().add("name", crawlerName).add("update", "crawlerUpdate");
    }

    @Before
    public void createViewsRepository() {
        modelRepository = new RecordsModelRepository(BaronGreenbackRecords.records(new MemoryRecords(new StringMappings().add(Model.class, new ModelMapping()))));
        viewsRepository = new ViewsRepository(modelRepository);
    }

}
