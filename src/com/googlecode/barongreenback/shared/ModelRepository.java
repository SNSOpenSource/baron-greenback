package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Records;

import java.util.UUID;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;

public class ModelRepository implements Repository<UUID, Model>{
    public static final Keyword<Object> MODELS = keyword("models");
    public static final Keyword<String> ID = keyword("models_id", String.class);
    public static final Keyword<String> MODEL = keyword("model", String.class);
    private final Records records;

    public ModelRepository(final Records records) {
        this.records = records;
        records.define(MODELS, ID, MODEL);
    }

    public Model get(UUID key) {
        return records.get(MODELS).filter(where(ID, is(key.toString()))).map(MODEL).map(asModel()).head();
    }

    public void set(UUID key, Model value) {
        records.add(MODELS, record().set(ID, key.toString()).set(MODEL, value.toString()));
    }

    private Callable1<? super String, Model> asModel() {
        return new Callable1<String, Model>() {
            public Model call(String value) throws Exception {
                return Model.parse(value);
            }
        };
    }


}
