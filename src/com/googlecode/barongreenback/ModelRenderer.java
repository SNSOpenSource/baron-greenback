package com.googlecode.barongreenback;

import com.googlecode.barongreenback.web.WebApplication;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.utterlyidle.Renderer;
import org.antlr.stringtemplate.StringTemplate;

import java.net.URI;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.URLs.packageUrl;

public class ModelRenderer implements Renderer<Model> {
    private final String name;

    public ModelRenderer(String name) {
        this.name = name;
    }

    public String render(Model value) throws Exception {
        EnhancedStringTemplateGroup group = new EnhancedStringTemplateGroup(packageUrl(WebApplication.class));
        group.registerRenderer(instanceOf(URI.class), toLink());
        StringTemplate template = group.getInstanceOf(name, value.toMap());
        return template.toString();
    }

    public static Callable1<URI, String> toLink() {
        return new Callable1<URI, String>() {
            public String call(URI uri) throws Exception {
                return String.format("<a href=\"%1$s\">%1$s</a>", uri);
            }
        };
    }
}
