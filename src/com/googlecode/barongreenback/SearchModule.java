package com.googlecode.barongreenback;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.lucene.LuceneRecords;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ResponseHandlers;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.utterlyidle.modules.ResponseHandlersModule;
import com.googlecode.yadic.Container;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.proxy.Call.method;
import static com.googlecode.totallylazy.proxy.Call.on;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.definedParam;
import static com.googlecode.utterlyidle.dsl.BindingBuilder.get;
import static com.googlecode.utterlyidle.dsl.DslBindings.binding;
import static com.googlecode.utterlyidle.handlers.HandlerRule.entity;
import static com.googlecode.utterlyidle.handlers.RenderingResponseHandler.renderer;

public class SearchModule implements ResourcesModule, ApplicationScopedModule, RequestScopedModule, ResponseHandlersModule {
    public Module addResources(Resources resources) throws ParseException {
        resources.add(annotatedClass(SearchResource.class));
        resources.add(binding(get("").resource(method(on(SearchResource.class).find(definedParam(""), definedParam(""))))));
        return this;
    }

    public Module addPerApplicationObjects(Container container) {
        container.addInstance(Version.class, Version.LUCENE_33);
        container.addActivator(Directory.class, DirectoryActivator.class);
        container.addActivator(IndexWriter.class, IndexWriterActivator.class);
        container.add(Analyzer.class, KeywordAnalyzer.class);
        return this;
    }

    public Module addPerRequestObjects(Container container) {
        container.add(LuceneRecords.class);
        container.addActivator(Records.class, container.getActivator(LuceneRecords.class));
        container.addActivator(QueryParser.class, QueryParserActivator.class);
        return this;
    }

    public Module addResponseHandlers(ResponseHandlers handlers) {
        handlers.add(where(entity(), is(instanceOf(Model.class))).and(where(file(), is("search"))), renderer(new ModelRenderer("search")));
        handlers.add(where(entity(), is(instanceOf(Model.class))).and(where(file(), is("unique"))), renderer(new ModelRenderer("unique")));
        return this;
    }

    public static Callable1<? super Pair<Request, Response>, String> file() {
        return new Callable1<Pair<Request, Response>, String>() {
            public String call(Pair<Request, Response> pair) throws Exception {
                return pair.first().url().path().file();
            }
        };
    }


}
