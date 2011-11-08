package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.ModelCleaner;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Second;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;

public class Views {
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = Keywords.keyword("group", String.class);
    public static final String ROOT = "view";

    public static Model clean(Model root) {
        return new ModelCleaner(in("view", "name", "query", "keywords", "group", "type", "unique", "visible")).clean(root);
    }

    public static Model convertToViewModel(Keyword<Object> recordName, Sequence<Keyword> keywords) {
        return model().add(ROOT, model().
                add("name", recordName.name()).
                add("query", "+type:\"" + recordName.name() + "\"").
                add("visible", true).
                add("keywords", keywords.map(asModel()).toList()));
    }

    private static Callable1<? super Keyword, Model> asModel() {
        return new Callable1<Keyword, Model>() {
            public Model call(Keyword keyword) throws Exception {
                Record metadata = keyword.metadata();
                return model().
                        add("name", keyword.name()).
                        add("group", metadata.get(Views.GROUP)).
                        add("type", keyword.forClass().getName()).
                        add("unique", metadata.get(Keywords.UNIQUE)).
                        add("visible", metadata.get(Views.VISIBLE));
            }
        };
    }

    public static Model view(Model definition) {
        return model().add("view", definition);
    }

    public static Option<Model> find(final ModelRepository modelRepository, final String name) {
        return modelRepository.find(Predicates.where(MODEL_TYPE, is(ROOT))).
                find(where(valueFor("name", String.class), is(name))).
                map(Callables.<Model>second());
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

    public static <T> Predicate<Second<Model>> where(Callable1<? super Model, T> callable1, Predicate<? super T> predicate) {
        return Predicates.where(second(Model.class),
                Predicates.where(unwrap(),
                        Predicates.where(callable1, predicate)));
    }
}
