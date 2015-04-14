package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class InMemoryRenderableTypesTest {

    @Test
    public void shouldBeAbleToRegisterRendererForAType() throws Exception {
        final DateRenderer dateRenderer = new DateRenderer("");
        final RenderableTypes renderableTypes = new InMemoryRenderableTypes().add(Date.class, dateRenderer);
        final List<Pair<Class<?>,Renderer<?>>> types = renderableTypes.renderableTypes();

        assertThat(types.size(), is(1));
        assertThat(types, contains(Pair.<Class<?>, Renderer<?>>pair(Date.class, dateRenderer)));
    }
}
