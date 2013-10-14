package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.Renderer;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.sitemesh.PageMap;
import com.googlecode.utterlyidle.sitemesh.TemplateName;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import static com.googlecode.totallylazy.Debug.debugging;
import static com.googlecode.totallylazy.Debug.inDebug;

public class ModelRenderer implements Renderer<Model> {
    private final StringTemplateGroup group;
    private final BasePath basePath;
    private final AdvancedMode mode;
    private final HttpClient httpHandlerForIncludes;
    private final TemplateName templateName;

    public ModelRenderer(StringTemplateGroup group, TemplateName templateName, BasePath basePath, AdvancedMode mode, HttpClient httpHandlerForIncludes) {
        this.templateName = templateName;
        this.group = group;
        this.basePath = basePath;
        this.mode = mode;
        this.httpHandlerForIncludes = httpHandlerForIncludes;
    }

    public String render(Model value) throws Exception {
        try {
            StringTemplate template = group.getInstanceOf(templateName.value(), value.toMap());
            template.setAttribute("base", basePath);
            template.setAttribute("include", new PageMap(httpHandlerForIncludes));
            template.setAttribute("advanced", mode.equals(AdvancedMode.Enable));
            template.setAttribute("debug", inDebug());
            return template.toString();
        } catch (Exception e) {
            if(debugging()) System.err.println("Could not load template '" +  templateName.value() + "' in " + ModelRenderer.class + " falling back to Model.toString()");
            return value.toString();
        }
    }
}
