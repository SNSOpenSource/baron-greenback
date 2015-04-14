package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;

import java.util.UUID;

import static com.googlecode.barongreenback.shared.RecordsModelRepository.toRecord;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.always;

public class CachingModelRepository implements ModelRepository {
    private final ModelRepository modelRepository;
    private final ModelCache cache;

    public CachingModelRepository(ModelRepository modelRepository, ModelCache cache) {
        this.modelRepository = modelRepository;
        this.cache = cache;
    }

    @Override
    public Sequence<Pair<UUID, Model>> find(final Predicate<? super Record> predicate) {
        makeSureCacheIsInitialised();
        Sequence<Pair<UUID, Model>> results = Maps.pairs(cache).filter(convert(predicate));
        return results.map(Callables.<UUID, Model, Model>second(copy()));
    }

    @Override
    public Option<Model> get(UUID key) {
        makeSureCacheIsInitialised();
        Option<Model> option = option(cache.get(key));
        if (option.isEmpty()) {
            return modelRepository.get(key).map(updateCache(key)).map(copy());
        }
        return option.map(copy());
    }

    private Function1<Model, Model> updateCache(final UUID key) {
        return new Function1<Model, Model>() {
            @Override
            public Model call(Model model) throws Exception {
                cache.put(key, model);
                return model;
            }
        };
    }

    public static Function1<Model, Model> copy() {
        return new Function1<Model, Model>() {
            @Override
            public Model call(Model model) throws Exception {
                return model.copy();
            }
        };
    }

    @Override
    public void set(UUID key, Model value) {
        makeSureCacheIsInitialised();
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

    private synchronized void makeSureCacheIsInitialised() {
        if (cache.isEmpty()) {
            Sequence<Pair<UUID, Model>> allModels = modelRepository.find(always());
            for (Pair<UUID, Model> model : allModels) {
                cache.put(model.first(), model.second());
            }
        }
    }
}
