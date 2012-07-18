package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.ModelCleaner;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Second;
import com.googlecode.totallylazy.numbers.Numbers;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;

public class Views {
    public static final Keyword<Boolean> VISIBLE = keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = keyword("group", String.class);
    public static final String ROOT = "view";

    public static Model clean(Model root) {
        return new ModelCleaner(in("view", "name", "records", "query", "priority", "keywords", "group", "type", "unique", "visible")).clean(root);
    }

    public static Model convertToViewModel(Definition definition) {
        String recordName = definition.name();
        return model().add(ROOT, model().
                add("name", recordName).
                add("records", recordName).
                add("query", "").
                add("visible", true).
                add("priority", "").
                add("keywords", definition.fields().map(asModel()).toList()));

    }

    public static Callable1<? super Keyword, Model> asModel() {
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

    public static Model copy(Model model) {
        Model copy = model.copy();
        Model root = copy.get(ROOT);
        String oldName = root.remove("name", String.class);
        root.add("name", "copy of " + oldName);
        root.remove("visible", Boolean.class);
        root.add("visible", false);
        return copy;
    }

    public static Callable1<? super Model, Model> unwrap() {
        return new Callable1<Model, Model>() {
            public Model call(Model model) throws Exception {
                return unwrap(model);
            }
        };
    }

    public static Model unwrap(Model model) {
        return model.get(ROOT, Model.class);
    }


    public static <T> Callable1<? super Model, T> valueFor(final String key, final Class<T> aClass) {
        return new Callable1<Model, T>() {
            public T call(Model model) throws Exception {
                return model.<T>get(key);
            }
        };
    }

    public static <T> Predicate<Second<Model>>
    where(Callable1<? super Model, T> callable1, Predicate<? super T> predicate) {
        return Predicates.where(second(Model.class),
                Predicates.where(unwrap(),
                        Predicates.where(callable1, predicate)));
    }

    public static String viewName(Model view) {
        return unwrap(view).get("records");
    }

    private static String name(Model model) {
        return model.get("name", String.class);
    }

    private static Double priority(Model model) {
        final Option<Number> priority = Numbers.valueOf(model.get("priority", String.class));
        return priority.isEmpty() ? Double.MAX_VALUE : priority.get().doubleValue();
    }

    public static Callable1<Model, Comparable> priority() {
        return new Callable1<Model, Comparable>() {
            public Comparable call(Model model) throws Exception {
                return priority(model);
            }
        };
    }

    public static Callable1<Model, Comparable> name() {
        return new Callable1<Model, Comparable>() {
            public Comparable call(Model model) throws Exception {
                return name(model);
            }
        };
    }

}
