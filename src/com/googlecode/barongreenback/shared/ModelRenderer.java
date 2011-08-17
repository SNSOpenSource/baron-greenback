package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import org.antlr.stringtemplate.StringTemplateGroup;

public class ModelRenderer implements Renderer<Model> {
    private final StringTemplateGroup group;
    private final TemplateName templateName;

    public ModelRenderer(StringTemplateGroup group, TemplateName templateName) {
        this.templateName = templateName;
        this.group = group;
    }

    public String render(Model value) throws Exception {
        return group.getInstanceOf(templateName.value(), value.toMap()).toString();
    }
}
