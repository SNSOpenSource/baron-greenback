package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Predicates;
import org.junit.Test;

import static com.googlecode.funclate.Model.model;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelFilterTest {
    
    @Test
    public void filtersEntriesByKey() throws Exception{
        ModelFilter filter = new ModelFilter(Predicates.is("bob"));
        assertThat(filter.filterModel(model().add("foo", "bar")), is(model()));
        assertThat(filter.filterModel(model().add("bob", "bar")), is(model().add("bob", "bar")));
    }

    @Test
    public void filtersNestedModelsEntriesByKey() throws Exception{
        ModelFilter filter = new ModelFilter(Predicates.is("root").or(Predicates.is("name")));
        Model nestedModel = model().add("root", model().
                add("name", "Dan").
                add("remove", "me") );
        assertThat(filter.filterModel(nestedModel), is(model().add("root", model().add("name", "Dan"))));
        assertThat(filter.filterModel(model().add("bob", "bar")), is(model()));
    }
}
