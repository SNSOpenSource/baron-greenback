package com.googlecode.barongreenback;

import com.googlecode.barongreenback.crawler.CrawlerModule;
import com.googlecode.barongreenback.jobs.JobsModule;
import com.googlecode.barongreenback.less.LessCssModule;
import com.googlecode.barongreenback.lucene.LuceneModule;
import com.googlecode.barongreenback.search.SearchModule;
import com.googlecode.barongreenback.shared.SharedModule;
import com.googlecode.barongreenback.views.ViewsModule;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.BasePath;
import com.googlecode.utterlyidle.RestApplication;
import com.googlecode.utterlyidle.ServerConfiguration;
import com.googlecode.utterlyidle.modules.Modules;
import com.googlecode.utterlyidle.modules.PerformanceModule;

import java.util.Properties;

import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
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
import static java.lang.System.getProperties;

public class WebApplication extends RestApplication {
    public WebApplication(BasePath basePath, Properties properties) {
        super(basePath);
        add(Modules.applicationInstance(properties));
        addModules(this);
        add(stringTemplateDecorators(packageUrl(SharedModule.class),
                metaTagRule("decorator"),
                staticRule(contentType(TEXT_HTML), templateName("decorator"))));
    }

    public static void addModules(Application application) {
        application.add(new LessCssModule());
        application.add(new PerformanceModule());
        application.add(new LuceneModule());
        application.add(new SharedModule());
        application.add(new CrawlerModule());
        application.add(new SearchModule());
        application.add(new ViewsModule());
        application.add(new JobsModule());
        application.add(bindingsModule(bindings(in(packageUrl(WebApplication.class)).path("baron-greenback").set("less", "text/css"))));
    }

    public static void main(String[] args) throws Exception {
        ServerConfiguration config = defaultConfiguration().port(9000);
        application(new WebApplication(config.basePath(), getProperties())).start(config);
    }
}
