package com.googlecode.barongreenback.persistence;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

public class InMemoryPersistentTypesActivatorTest {

    @Test
    public void shouldIncludeMappingForStringDateAndURI() throws Exception {
        final InMemoryPersistentTypesActivator inMemoryPersistentTypesActivator = new InMemoryPersistentTypesActivator();
        final PersistentTypes persistentTypes = inMemoryPersistentTypesActivator.call();
        assertThat(persistentTypes.types(), CoreMatchers.<Class<?>>hasItems(String.class, Date.class, URI.class));
    }
}
