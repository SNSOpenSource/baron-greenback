package com.googlecode.barongreenback.shared;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.io.HierarchicalPath;
import com.googlecode.utterlyidle.sitemesh.TemplateName;

import java.util.concurrent.Callable;

import static com.googlecode.utterlyidle.sitemesh.TemplateName.templateName;
import static java.lang.String.format;

public class TemplateNameActivator implements Callable<TemplateName> {
    private final HierarchicalPath path;

    public TemplateNameActivator(Request request) {
        path = request.url().path();
    }

    public TemplateName call() throws Exception {
        return templateName(path.file());
    }
}
