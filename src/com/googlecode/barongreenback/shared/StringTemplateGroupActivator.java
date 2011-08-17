package com.googlecode.barongreenback.shared;


import com.googlecode.barongreenback.WebApplication;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.URLs.url;

public class StringTemplateGroupActivator implements Callable<StringTemplateGroup>{
    public StringTemplateGroup call() throws Exception {
        URL templates = packageUrl(WebApplication.class);
        EnhancedStringTemplateGroup shared = new EnhancedStringTemplateGroup(subDirectory(templates, "shared"));
        EnhancedStringTemplateGroup group = new EnhancedStringTemplateGroup(templates);
        group.setSuperGroup(shared);
        group.registerRenderer(instanceOf(URI.class), URIRenderer.toLink());
        return group;
    }

    private URL subDirectory(URL base, String directory) {
        return url(base.toString() + directory);
    }
}
