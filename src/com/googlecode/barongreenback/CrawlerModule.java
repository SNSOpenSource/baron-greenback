package com.googlecode.barongreenback;

import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.ResourcesModule;

import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.utterlyidle.dsl.DslBindings.bindings;
import static com.googlecode.utterlyidle.dsl.StaticBindingBuilder.in;

public class CrawlerModule implements ResourcesModule {
    public Module addResources(Resources resources) {
        resources.add(bindings(in(packageUrl(WebApplication.class)).path("static")));
        return this;
    }
}
