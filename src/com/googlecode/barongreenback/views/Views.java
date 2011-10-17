package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class Views {
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = Keywords.keyword("group", String.class);
    public static final String ROOT = "view";

    public static Model clean(Model root) {
        return view(convert(root.get(ROOT, Model.class)).toModel());
    }

    public static Model view(Keyword<Object> recordName, Sequence<Keyword> keywords) {
        return view(new RecordDefinition(recordName, keywords));
    }

    public static Model view(RecordDefinition recordDefinition) {
        return view(recordDefinition.toModel());
    }

    public static Model view(Model definition) {
        return model().add("view", definition);
    }

    public static Option<Model> find(final ModelRepository modelRepository, final String name) {
        return modelRepository.find(where(MODEL_TYPE, is(ROOT))).
                map(Callables.<Model>second()).
                find(where(unwrap(), where(valueFor("name", String.class), is(name))));
    }

    public static Callable1<? super Model, Model> unwrap() {
        return new Callable1<Model, Model>() {
            public Model call(Model model) throws Exception {
                return model.get(ROOT, Model.class);
            }
        };
    }

    public static <T> Callable1<? super Model, T> valueFor(final String key, final Class<T> aClass) {
        return new Callable1<Model, T>() {
            public T call(Model model) throws Exception {
                return model.<T>get(key);
            }
        };
    }

}
