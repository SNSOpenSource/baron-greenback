package com.googlecode.barongreenback;

import com.googlecode.barongreenback.crawler.CrawlerModule;
import com.googlecode.barongreenback.jobs.JobsModule;
import com.googlecode.barongreenback.search.SearchModule;
import com.googlecode.barongreenback.shared.SharedModule;
import com.googlecode.barongreenback.views.ViewsModule;
import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.httpserver.RestServer;

import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.utterlyidle.MediaType.TEXT_HTML;
import static com.googlecode.utterlyidle.ServerConfiguration.defaultConfiguration;
import static com.googlecode.utterlyidle.dsl.DslBindings.bindings;
import static com.googlecode.utterlyidle.dsl.StaticBindingBuilder.in;
import static com.googlecode.utterlyidle.modules.Modules.bindingsModule;
import static com.googlecode.utterlyidle.sitemesh.ContentTypePredicate.contentType;
import static com.googlecode.utterlyidle.sitemesh.MetaTagRule.metaTagRule;
import static com.googlecode.utterlyidle.sitemesh.StaticDecoratorRule.staticRule;
import static com.googlecode.utterlyidle.sitemesh.StringTemplateDecorators.stringTemplateDecorators;
import static com.googlecode.utterlyidle.sitemesh.TemplateName.templateName;

public class WebApplication extends RestApplication {
    public WebApplication() {
        add(new SharedModule());
        add(new CrawlerModule());
        add(new SearchModule());
        add(new ViewsModule());
        add(new JobsModule());
        add(stringTemplateDecorators(packageUrl(SharedModule.class),
                metaTagRule("decorator"),
                staticRule(contentType(TEXT_HTML), templateName("twitter"))));
        add(bindingsModule(bindings(in(packageUrl(WebApplication.class)).path("").set("less", "text/css"))));
    }

    public static void main(String[] args) throws Exception {
        new RestServer(new WebApplication(), defaultConfiguration().port(9000));
    }
}
