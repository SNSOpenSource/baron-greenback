package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import static com.googlecode.barongreenback.shared.ModelRepository.MODEL_TYPE;
import static com.googlecode.funclate.Model.functions.value;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.blank;
import static com.googlecode.totallylazy.numbers.Numbers.sum;
import static com.googlecode.totallylazy.predicates.Not.not;

public class PrimaryViewShortcutPolicy implements ShortcutPolicy {
    private final RecordsService recordsService;
    private final ModelRepository modelRepository;

    public PrimaryViewShortcutPolicy(RecordsService recordsService, ModelRepository modelRepository) {
        this.recordsService = recordsService;
        this.modelRepository = modelRepository;
    }

    @Override
    public boolean shouldShortcut(String view, String query) {
        Sequence<Pair<String, Number>> counts = views().filter(is(visible()).and(hasNoParent().or(hasParentIn(invisibleParents())))).
                map(value("name", String.class)).
                map(toCount(query).capturing()).memoize();

        Number count = counts.
                map(Callables.<Number>second()).
                reduce(sum());

        return counts.exists(Predicates.is(Pair.pair(view, Numbers.number(1)))) && count.intValue() == 1;
    }

    private Sequence<Model> views() {
        return modelRepository.find(where(MODEL_TYPE, is("view"))).
                map(Callables.<Model>second()).
                map(value("view", Model.class));
    }

    private LogicalPredicate<Model> visible() {
        return where(value("visible", Boolean.class), is(true));
    }

    private LogicalPredicate<Model> hasNoParent() {
        return where(value("parent", String.class), is(blank()));
    }

    private LogicalPredicate<Model> hasParentIn(Sequence<String> parents) {
        return where(value("parent", String.class), in(parents));
    }

    private Sequence<String> invisibleParents() {
        return views().filter(hasNoParent()).filter(is(not(visible()))).map(value("name", String.class));
    }

    private Function1<String, Number> toCount(final String query) {
        return new Function1<String, Number>() {
            @Override
            public Number call(String viewName) throws Exception {
                return recordsService.count(viewName, query);
            }
        };
    }
}
