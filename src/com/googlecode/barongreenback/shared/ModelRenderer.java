package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

public class ModelRenderer implements Renderer<Model> {
    private final StringTemplateGroup group;
    private final BasePath basePath;
    private final TemplateName templateName;

    public ModelRenderer(StringTemplateGroup group, TemplateName templateName, BasePath basePath) {
        this.templateName = templateName;
        this.group = group;
        this.basePath = basePath;
    }

    public String render(Model value) throws Exception {
        StringTemplate template = group.getInstanceOf(templateName.value(), value.toMap());
        template.setAttribute("base", basePath);
        return template.toString();
    }
}
