package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordName;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.MapRecord.record;
import static com.googlecode.lazyrecords.RecordMethods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.first;

public class ModelRepository implements Repository<UUID, Model>, Finder<Pair<UUID, Model>> {
    public static final RecordName MODELS = RecordName.recordName("models");
    public static final Keyword<UUID> ID = keyword("models_id", UUID.class);
    public static final Keyword<String> MODEL_TYPE = keyword("models_type", String.class);
    public static final Keyword<Model> MODEL = keyword("model", Model.class);
    private final Records records;

    public ModelRepository(final BaronGreenbackRecords records) {
        this.records = records.value();
        this.records.define(MODELS, ID, MODEL, MODEL_TYPE);
    }

    public Option<Model> get(UUID key) {
        return find(where(ID, is(key))).map(second(Model.class)).headOption();
    }

    public Sequence<Pair<UUID, Model>> find(Predicate<? super Record> predicate) {
        return records.get(MODELS).filter(predicate).map(asPair());
    }

    private Callable1<Record, Pair<UUID, Model>> asPair() {
        return new Callable1<Record, Pair<UUID, Model>>() {
            public Pair<UUID, Model> call(Record record) throws Exception {
                return Pair.pair(record.get(ID), record.get(MODEL));
            }
        };
    }

    public void set(UUID key, Model value) {
        records.put(MODELS, update(using(ID), record().set(ID, key).set(MODEL_TYPE, modelType(value)).set(MODEL, value)));
    }

    private String modelType(Model model) {
        return first(model.entries()).getKey();
    }

    public void remove(UUID key) {
        records.remove(MODELS, where(ID, is(key)));
    }
}
