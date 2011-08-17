package com.googlecode.barongreenback.shared;


import com.googlecode.barongreenback.WebApplication;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.URLs;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.io.HierarchicalPath;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Strings.EMPTY;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.URLs.url;

public class StringTemplateGroupActivator implements Callable<StringTemplateGroup> {
    private final HierarchicalPath path;

    public StringTemplateGroupActivator(final Request request) {
        this.path = request.url().path();
    }

    public StringTemplateGroup call() throws Exception {
        EnhancedStringTemplateGroup shared = new EnhancedStringTemplateGroup(URLs.packageUrl(SharedModule.class));
        shared.registerRenderer(instanceOf(URI.class), URIRenderer.toLink());

        EnhancedStringTemplateGroup group = new EnhancedStringTemplateGroup(append(packageUrl(WebApplication.class), packageName(path)));
        group.setSuperGroup(shared);
        return group;
    }

    private URL append(URL url, String path) {
        return url(url.toString() + path);
    }

    private static String packageName(HierarchicalPath path) {
        return path.segments().reverse().second();
    }

}
