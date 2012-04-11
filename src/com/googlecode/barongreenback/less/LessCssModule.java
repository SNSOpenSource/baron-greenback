package com.googlecode.barongreenback.less;

import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.yadic.Container;

import java.io.File;
import java.util.concurrent.Callable;

public class LessCssModule implements RequestScopedModule, ApplicationScopedModule {
    public Module addPerApplicationObjects(Container container) throws Exception {
        container.add(LessCompiler.class, RhinoLessCompiler.class);
        container.add(LessCssCache.class, InMemoryLessCssCache.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
        container.add(LessCssConfig.class, CacheUnlessInDebug.class);
        container.decorate(HttpHandler.class, LessCssHandler.class);
        return this;
    }
}
