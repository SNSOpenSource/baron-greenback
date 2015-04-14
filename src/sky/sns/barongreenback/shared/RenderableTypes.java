package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Pair;

import java.util.List;

public interface RenderableTypes {

    <T> RenderableTypes add(Class<T> klass, Renderer<T> renderer);

    List<Pair<Class<?>, Renderer<?>>> renderableTypes();
}
