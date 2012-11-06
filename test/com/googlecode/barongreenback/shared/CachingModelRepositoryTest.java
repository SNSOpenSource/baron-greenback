package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.ModelMapping;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class CachingModelRepositoryTest {
    private Records records;
    private ModelCache cache;
    private ModelRepository cachingModelRepository;
    private Model model;
    private UUID key;

    @Before
    public void setUp() throws Exception {
        records = new MemoryRecords(new StringMappings().add(Model.class, new ModelMapping()));
        cache = new ModelCache();
        cachingModelRepository = new CachingModelRepository(new RecordsModelRepository(BaronGreenbackRecords.records(records)), cache);
        model = model().add("key", "someValue");
        key = UUID.randomUUID();
    }

    @Test
    public void shouldAlwaysReturnACopyOfTheModel() throws Exception {
        cachingModelRepository.set(key, model);
        assertThat(cachingModelRepository.get(key).get(), not(sameInstance(model)));
        assertThat(cachingModelRepository.find(where(ModelRepository.MODEL, Predicates.is(model))).head().second(), not(sameInstance(model)));
    }

    @Test
    public void setShouldWriteThroughCacheToUnderlyingRepository() throws Exception {
        cachingModelRepository.set(key, model);
        assertThat(cache.get(key), is(model));
        assertThat(records.get(ModelRepository.MODELS).head().get(ModelRepository.MODEL), is(model));
    }

    @Test
    public void removeShouldUpdateCacheAndRepository() throws Exception {
        cachingModelRepository.set(key, model);
        cachingModelRepository.remove(key);
        assertThat(cache.containsKey(key), is(false));
        assertThat(records.get(ModelRepository.MODELS).isEmpty(), is(true));
    }

    @Test
    public void getShouldRetrieveValueFromCache() throws Exception {
        cachingModelRepository.set(key, model);
        Option<Model> result = cachingModelRepository.get(key);
        assertThat(result.get(), is(model));
    }

    @Test
    public void findShouldRetrieveValuesFromCache() throws Exception {
        cachingModelRepository.set(key, model);
        Pair<UUID, Model> result = cachingModelRepository.find(where(ModelRepository.MODEL, Predicates.is(model))).head();
        assertThat(result.first(), is(key));
        assertThat(result.second(), is(model));
    }

    @Test
    public void shouldUpdateCacheIfCacheMissWhenFinding() throws Exception {
        cachingModelRepository.set(key, model);
        cache.clear();
        Pair<UUID, Model> result = cachingModelRepository.find(where(ModelRepository.MODEL, Predicates.is(model))).head();
        assertThat(result.first(), is(key));
        assertThat(result.second(), is(model));
        assertThat(cache.containsKey(key), is(true));
        assertThat(cache.containsValue(model), is(true));

    }

    @Test
    public void shouldUpdateCacheIfCacheMissWhenGetting() throws Exception {
        cachingModelRepository.set(key, model);
        cache.clear();
        Model result = cachingModelRepository.get(key).get();
        assertThat(result, is(model));
        assertThat(cache.containsKey(key), is(true));
        assertThat(cache.containsValue(model), is(true));

    }


}
