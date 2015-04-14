package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.mappings.ObjectMapping;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;

public class InMemoryPersistentTypesTest {

    @Test
    public void shouldReturnAllTheRegisteredTypes() throws Exception {
        final PersistentTypes persistentTypes = new InMemoryPersistentTypes().add(String.class).add(Integer.class, new ObjectMapping<Integer>(Integer.class));

        final List<Class<?>> availableTypes = persistentTypes.types();

        assertThat(availableTypes.size(), CoreMatchers.is(2));
        assertThat(availableTypes, CoreMatchers.<Class<?>>hasItems(String.class, Integer.class));
    }

    @Test
    public void shouldReturnMappingsOnlyForTypesWithAMapping() throws Exception {
        final ObjectMapping<Integer> mapping = new ObjectMapping<Integer>(Integer.class);
        final PersistentTypes persistentTypes = new InMemoryPersistentTypes().add(String.class).add(Integer.class, mapping);

        final List<Pair<Class<?>, Callable1<StringMappings,StringMapping>>> mappings = persistentTypes.mappings();

        assertThat(mappings.size(), CoreMatchers.is(1));
        assertThat(mappings.get(0).first(), CoreMatchers.<Class<?>>is(Integer.class));
        assertThat(mappings.get(0).second().call(null), CoreMatchers.<StringMapping>is(mapping));
    }

    @Test
    public void shouldAllowAddingAMappingThatDependsOnAnotherMapping() throws Exception {
        final Callable1<StringMappings,StringMapping<Integer>> mappingFunction = Callables.<StringMappings,StringMapping<Integer>>returns1(new ObjectMapping<Integer>(Integer.class));
        final PersistentTypes persistentTypes = new InMemoryPersistentTypes().add(String.class).add(Integer.class, mappingFunction);

        final List<Pair<Class<?>, Callable1<StringMappings,StringMapping>>> mappings = persistentTypes.mappings();

        assertThat(mappings.size(), CoreMatchers.is(1));
        assertThat(mappings.get(0).first(), CoreMatchers.<Class<?>>is(Integer.class));
        assertThat(mappings.get(0).second(), is(mappingFunction));
    }

    private BaseMatcher<Callable1<StringMappings, StringMapping>> is(final Callable1<StringMappings, StringMapping<Integer>> mappingFunction) {
        return new BaseMatcher<Callable1<StringMappings, StringMapping>>() {
            @Override
            public boolean matches(Object o) {
                return o.equals(mappingFunction);
            }

            @Override
            public void describeTo(Description description) {
                throw new RuntimeException("not done yet");
            }
        };
    }
}
