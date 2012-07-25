package com.googlecode.barongreenback.shared;


import com.googlecode.barongreenback.WebApplication;
import com.googlecode.barongreenback.shared.pager.PagerRenderer;
import com.googlecode.barongreenback.shared.pager.RequestPager;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.URLs;
import com.googlecode.totallylazy.Xml;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.io.HierarchicalPath;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Predicates.always;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.URLs.url;

public class StringTemplateGroupActivator implements Callable<StringTemplateGroup> {
    private final URL baseUrl;

    public StringTemplateGroupActivator(final Request request) {
        baseUrl = append(packageUrl(WebApplication.class), packageName(HierarchicalPath.hierarchicalPath(request.uri().path())));
    }

    public StringTemplateGroupActivator(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StringTemplateGroup call() throws Exception {
        EnhancedStringTemplateGroup shared = new EnhancedStringTemplateGroup(URLs.packageUrl(SharedModule.class));
        shared.enableFormatsAsFunctions();
        shared.registerRenderer("underscores", instanceOf(String.class), underscores());
        shared.registerRenderer("dashes", instanceOf(String.class), dashes());
        shared.registerRenderer(always(), Xml.escape());
        shared.registerRenderer(instanceOf(URI.class), URIRenderer.toLink());
        shared.registerRenderer(instanceOf(Date.class), DateRenderer.toLexicalDateTime());
        shared.registerRenderer(instanceOf(RequestPager.class), PagerRenderer.pagerRenderer(shared));
        return new EnhancedStringTemplateGroup(baseUrl, shared);
    }

    public static Function1<String, String> underscores() {
        return new Function1<String, String>(){
            @Override
            public String call(String s) throws Exception {
                return s.replace(' ', '_');
            }
        };
    }
    public static Function1<String, String> dashes() {
        return new Function1<String, String>(){
            @Override
            public String call(String s) throws Exception {
                return s.replace(' ', '-');
            }
        };
    }

    static URL append(URL url, String path) {
        return url(url.toString() + path);
    }

    private static String packageName(HierarchicalPath path) {
        return path.segments().init().last();
    }



}
