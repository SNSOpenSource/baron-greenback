package com.googlecode.barongreenback.shared;


import com.googlecode.barongreenback.shared.pager.PagerRenderer;
import com.googlecode.barongreenback.shared.pager.RequestPager;
import com.googlecode.funclate.Renderer;
import com.googlecode.funclate.stringtemplate.EnhancedStringTemplateGroup;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.HtmlEncodedMessage;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.URLs;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Xml;
import com.googlecode.totallylazy.regex.Regex;
import com.googlecode.utterlyidle.MatchedResource;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Functions.returns1;
import static com.googlecode.totallylazy.Predicates.always;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.URLs.packageUrl;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.UrlEncodedMessage.encode;
import static com.googlecode.totallylazy.regex.Regex.regex;

public class StringTemplateGroupActivator implements Callable<StringTemplateGroup> {
    public static final Regex UI_ROOT = regex("^.*\\/utterlyidle\\/");
    public static final Regex BG_ROOT = regex("^.*\\/barongreenback\\/");

    private final URL baseUrl;
    private final RenderableTypes renderableTypes;

    public StringTemplateGroupActivator(final MatchedResource matchedResource, RenderableTypes renderableTypes) {
        this(makeRelativeToBaronGreenback(packageUrl(matchedResource.forClass())), renderableTypes);
    }

    public StringTemplateGroupActivator(URL baseUrl, RenderableTypes renderableTypes) {
        this.baseUrl = baseUrl;
        this.renderableTypes = renderableTypes;
    }

    private static URL makeRelativeToBaronGreenback(URL url) {
        String bgRoot = BG_ROOT.match(packageUrl(StringTemplateGroupActivator.class).toString()).group(0);
        return url(UI_ROOT.findMatches(url.toString()).replace(returns1(bgRoot)));
    }

    public StringTemplateGroup call() throws Exception {
        EnhancedStringTemplateGroup shared = new EnhancedStringTemplateGroup(URLs.packageUrl(SharedModule.class));
        shared.enableFormatsAsFunctions();
        shared.registerRenderer("underscores", instanceOf(String.class), underscores());
        shared.registerRenderer("dashes", instanceOf(String.class), dashes());
        shared.registerRenderer(always(), Xml.escape());
        shared.registerRenderer(instanceOf(RequestPager.class), PagerRenderer.pagerRenderer(shared));
        shared.registerRenderer("htmlDecode", Predicates.<Object>always(), HtmlEncodedMessage.functions.decode());
        shared.registerRenderer("urlEncode", Predicates.<Object>always(), encodeUrl());
        sequence(renderableTypes.renderableTypes()).forEach(registerRenderer(shared));
        return new EnhancedStringTemplateGroup(baseUrl, shared);
    }

    private Block<Pair<Class<?>, Renderer<?>>> registerRenderer(final EnhancedStringTemplateGroup stringTemplateGroup) {
        return new Block<Pair<Class<?>, Renderer<?>>>() {
            @Override
            protected void execute(Pair<Class<?>, Renderer<?>> rendererPair) throws Exception {
                stringTemplateGroup.registerRenderer(instanceOf(rendererPair.first()), Unchecked.<Renderer>cast(rendererPair.second()));
            }
        };
    }

    private Callable1<Object, String> encodeUrl() {
        return new Callable1<Object, String>() {
            @Override
            public String call(Object value) throws Exception {
                return encode(Strings.asString(value));
            }
        };
    }

    public static Function1<String, String> underscores() {
        return new Function1<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s.replace(' ', '_');
            }
        };
    }

    public static Function1<String, String> dashes() {
        return new Function1<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s.replace(' ', '-');
            }
        };
    }

    static URL append(URL url, String path) {
        return url(url.toString() + path);
    }
}
