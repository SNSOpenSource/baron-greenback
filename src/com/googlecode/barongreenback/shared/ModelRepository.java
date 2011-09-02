package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;

import java.util.UUID;

import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;

public class ModelRepository implements Repository<UUID, Model>, Finder<Pair<UUID, Model>> {
    public static final Keyword<Object> MODELS = keyword("models");
    public static final Keyword<String> ID = keyword("models_id", String.class);
    public static final Keyword<String> MODEL = keyword("model", String.class);
    private final Records records;

    public ModelRepository(final Records records) {
        this.records = records;
        records.define(MODELS, ID, MODEL);
    }

    public Model get(UUID key) {
        return find(where(ID, is(key.toString()))).map(second(Model.class)).head();
    }

    public Sequence<Pair<UUID, Model>> find(Predicate<? super Record> predicate) {
        return records.get(MODELS).filter(predicate).map(new Callable1<Record, Pair<UUID, Model>>() {
            public Pair<UUID, Model> call(Record record) throws Exception {
                return Pair.pair(UUID.fromString(record.get(ID)), Model.parse(record.get(MODEL)));
            }
        });
    }

    public void set(UUID key, Model value) {
        records.put(MODELS, update(using(ID), record().set(ID, key.toString()).set(MODEL, value.toString())));
    }

    public void remove(UUID key) {
        records.remove(MODELS, where(ID, is(key.toString())));
    }
}
