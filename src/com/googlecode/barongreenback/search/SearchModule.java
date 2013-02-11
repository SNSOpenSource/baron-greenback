package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.barongreenback.shared.pager.RequestPager;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.funclate.Funclate;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.StringFunclate;
import com.googlecode.lazyrecords.parser.ParametrizedParser;
import com.googlecode.lazyrecords.parser.ParserParameters;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.lazyrecords.parser.StandardParser;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.handlers.ConvertExtensionToAcceptHeader;
import com.googlecode.utterlyidle.modules.ModuleDefiner;
import com.googlecode.utterlyidle.modules.ModuleDefinitions;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;

import java.util.Map;
import java.util.Properties;

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.ConvertExtensionToAcceptHeader.Replacements.replacements;

public class SearchModule implements ParserParametersModule, ResourcesModule, RequestScopedModule, ModuleDefiner {
    public Resources addResources(Resources resources) {
        return resources.add(annotatedClass(SearchResource.class));
    }

    public Container addPerRequestObjects(Container container) throws Exception {
		container.add(Pager.class, RequestPager.class).
                add(Sorter.class, Sorter.class).
                add(PredicateParser.class, StandardParser.class).
                decorate(PredicateParser.class, ParametrizedParser.class).
                add(PredicateBuilder.class).
                add(ParserParameters.class).
                add(Properties.class);
        return Containers.addInstanceIfAbsent(container, ConvertExtensionToAcceptHeader.Replacements.class, replacements(Pair.pair("json", MediaType.APPLICATION_JSON))).
                decorate(HttpHandler.class, ConvertExtensionToAcceptHeader.class);
    }

    public ModuleDefinitions defineModules(ModuleDefinitions moduleDefinitions) throws Exception {
        return moduleDefinitions.addRequestModule(ParserParametersModule.class);
    }

    @Override
    public ParserParameters addParameters(ParserParameters parameters, Container container) {
        final Properties properties = container.get(Properties.class);
        return parameters.add("properties", StringFunclate.functions.first(new UnaryFunction<String>() {
            @Override
            public String call(String key) throws Exception {
                return properties.getProperty(key);
            }
        }));
    }
}
