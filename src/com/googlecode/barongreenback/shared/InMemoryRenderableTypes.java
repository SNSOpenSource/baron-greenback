package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.collections.PersistentMap;

import java.util.List;

import static com.googlecode.totallylazy.collections.HashTreeMap.hashTreeMap;

public class InMemoryRenderableTypes implements RenderableTypes {
    private final PersistentMap<Class<?>, Renderer<?>> map;

    public InMemoryRenderableTypes() {
        this.map = hashTreeMap();
    }

    private InMemoryRenderableTypes(PersistentMap<Class<?>, Renderer<?>> map) {
        this.map = map;
    }

    @Override
    public <T> RenderableTypes add(Class<T> klass, Renderer<T> renderer) {
        return new InMemoryRenderableTypes(map.insert(klass, renderer));
    }

    @Override
    public List<Pair<Class<?>, Renderer<?>>> renderableTypes() {
        return map.toPersistentList();
    }
}
