package com.googlecode.barongreenback.persistence;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

public class InMemoryTypesActivatorTest {

    @Test
    public void shouldIncludeMappingForStringDateAndURI() throws Exception {
        final InMemoryTypesActivator inMemoryTypesActivator = new InMemoryTypesActivator();
        final Types types = inMemoryTypesActivator.call();
        assertThat(types.types(), CoreMatchers.<Class<?>>hasItems(String.class, Date.class, URI.class));
    }
}
