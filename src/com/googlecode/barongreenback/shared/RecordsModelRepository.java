package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.first;

public class RecordsModelRepository implements ModelRepository {
    private final Records records;

    public RecordsModelRepository(final BaronGreenbackRecords records) {
        this.records = records.value();
    }

    public Sequence<Pair<UUID, Model>> find(Predicate<? super Record> predicate) {
        return records.get(MODELS).filter(predicate).map(asPair());
    }

    public Option<Model> get(UUID key) {
        return find(where(ID, is(key))).map(second(Model.class)).headOption();
    }

    public void set(UUID key, Model value) {
        records.put(MODELS, update(using(ID), toRecord(key, value)));
    }

    public void remove(UUID key) {
        records.remove(MODELS, where(ID, is(key)));
    }

    public static Record toRecord(UUID key, Model value) {
        return record().set(ID, key).set(MODEL_TYPE, modelType(value)).set(MODEL, value);
    }

    private Callable1<Record, Pair<UUID, Model>> asPair() {
        return new Callable1<Record, Pair<UUID, Model>>() {
            public Pair<UUID, Model> call(Record record) throws Exception {
                return Pair.pair(record.get(ID), record.get(MODEL));
            }
        };
    }

    private static String modelType(Model model) {
        return first(model.entries()).getKey();
    }
}
