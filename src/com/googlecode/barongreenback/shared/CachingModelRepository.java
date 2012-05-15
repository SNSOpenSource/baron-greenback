package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.RecordsModelRepository.toRecord;
import static com.googlecode.totallylazy.Option.option;

public class CachingModelRepository implements ModelRepository {
    private final ModelRepository modelRepository;
    private final ModelCache cache;

    public CachingModelRepository(ModelRepository modelRepository, ModelCache cache) {
        this.modelRepository = modelRepository;
        this.cache = cache;
    }

    @Override
    public Sequence<Pair<UUID, Model>> find(final Predicate<? super Record> predicate) {
        Sequence<Pair<UUID, Model>> results = Maps.pairs(cache).filter(convert(predicate));
        if(results.isEmpty()){
            Sequence<Pair<UUID, Model>> pairs = modelRepository.find(predicate);
            for (Pair<UUID, Model> pair : pairs) {
                cache.put(pair.first(), pair.second());
            }
            return pairs;
        }
        return results;
    }

    @Override
    public Option<Model> get(UUID key) {
        Option<Model> option = option(cache.get(key));
        if(option.isEmpty()) {
            Option<Model> models = modelRepository.get(key);
            for (Model model : models) {
                cache.put(key, model);
            }
            return models;
        }
        return option;
    }

    @Override
    public void set(UUID key, Model value) {
        cache.put(key, value);
        modelRepository.set(key, value);
    }

    @Override
    public void remove(UUID key) {
        cache.remove(key);
        modelRepository.remove(key);
    }

    private Predicate<Pair<UUID, Model>> convert(final Predicate<? super Record> predicate) {
        return new Predicate<Pair<UUID, Model>>() {
            @Override
            public boolean matches(Pair<UUID, Model> pair) {
                return predicate.matches(toRecord(pair.first(), pair.second()));
            }
        };
    }
}
