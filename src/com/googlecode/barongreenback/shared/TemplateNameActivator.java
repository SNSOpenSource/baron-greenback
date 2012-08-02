package com.googlecode.barongreenback.shared;

import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.io.HierarchicalPath;
import com.googlecode.utterlyidle.sitemesh.TemplateName;

import java.util.concurrent.Callable;

import static com.googlecode.utterlyidle.io.HierarchicalPath.hierarchicalPath;
import static com.googlecode.utterlyidle.sitemesh.TemplateName.templateName;

public class TemplateNameActivator implements Callable<TemplateName> {
    private final HierarchicalPath path;

    public TemplateNameActivator(Request request) {
        path = hierarchicalPath(request.uri().path());
    }

    public TemplateName call() throws Exception {
        return templateName(path.file());
    }
}
