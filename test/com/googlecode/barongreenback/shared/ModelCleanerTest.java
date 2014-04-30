package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Predicates;
import org.junit.Test;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Predicates.in;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelCleanerTest {

    @Test
    public void canFilterAView() throws Exception {
        ModelCleaner cleaner = new ModelCleaner(in("view", "name", "query", "keywords", "group", "type", "unique", "visible"));
        Model messy = model().add("view", model().
                add("keywords", model().
                        add("record", "should be removed").
                        add("name", "should stay")).
                add("keywords", model().
                        add("record", "should be removed").
                        add("name", "should stay")));
        Model clean = model().add("view", model().
                add("keywords", model().
                        add("name", "should stay")).
                add("keywords", model().
                        add("name", "should stay")));
        assertThat(cleaner.clean(messy), is(clean));
    }

    @Test
    public void filtersEntriesByKey() throws Exception {
        ModelCleaner cleaner = new ModelCleaner(Predicates.is("bob"));
        assertThat(cleaner.clean(model().add("foo", "bar")), is(model()));
        assertThat(cleaner.clean(model().add("bob", "bar")), is(model().add("bob", "bar")));
    }

    @Test
    public void filtersNestedModelsEntriesByKey() throws Exception {
        ModelCleaner cleaner = new ModelCleaner(Predicates.is("root").or(Predicates.is("name")));
        Model nestedModel = model().add("root", model().
                add("name", "Dan").
                add("remove", "me"));
        assertThat(cleaner.clean(nestedModel), is(model().add("root", model().add("name", "Dan"))));
        assertThat(cleaner.clean(model().add("bob", "bar")), is(model()));
    }
}
