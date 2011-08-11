package com.googlecode.barongreenback;

import com.googlecode.barongreenback.web.WebApplication;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.utterlyidle.Renderer;
import org.antlr.stringtemplate.StringTemplate;

import java.net.URI;
import java.net.URL;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.URLs.packageUrl;

public class ModelRenderer implements Renderer<Model> {
    private final String name;
    private final URL templates;

    public ModelRenderer(String name) {
        this.name = name;
        templates = packageUrl(WebApplication.class);
    }

    public String render(Model value) throws Exception {
        EnhancedStringTemplateGroup group = new EnhancedStringTemplateGroup(templates);
        group.registerRenderer(instanceOf(URI.class), URIRenderer.toLink());
        StringTemplate template = group.getInstanceOf(name, value.toMap());
        return template.toString();
    }

}
