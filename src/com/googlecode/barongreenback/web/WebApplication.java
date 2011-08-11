package com.googlecode.barongreenback.web;

import com.googlecode.barongreenback.SearchModule;
import com.googlecode.barongreenback.ViewsModule;
import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.httpserver.RestServer;

import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static com.googlecode.utterlyidle.dsl.DslBindings.bindings;
import static com.googlecode.utterlyidle.dsl.StaticBindingBuilder.in;
import static com.googlecode.utterlyidle.modules.Modules.bindingsModule;
import static com.googlecode.utterlyidle.sitemesh.ContentTypePredicate.contentType;
import static com.googlecode.utterlyidle.sitemesh.StaticDecoratorRule.staticRule;
import static com.googlecode.utterlyidle.sitemesh.StringTemplateDecorators.stringTemplateDecorators;
import static com.googlecode.utterlyidle.sitemesh.TemplateName.templateName;

public class WebApplication extends RestApplication {
    public WebApplication() {
        add(new SearchModule());
        add(new ViewsModule());
        add(stringTemplateDecorators(packageUrl(WebApplication.class), staticRule(contentType(TEXT_HTML), templateName("default"))));
        add(bindingsModule(bindings(in(packageUrl(WebApplication.class)).path(""))));
    }

    public static void main(String[] args) throws Exception {
        new RestServer(new WebApplication(), defaultConfiguration().port(9000));
    }
}
