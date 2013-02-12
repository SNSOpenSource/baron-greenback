package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.shared.pager.Pager;
import com.googlecode.barongreenback.shared.pager.RequestPager;
import com.googlecode.barongreenback.shared.sorter.Sorter;
import com.googlecode.funclate.Funclate;
import com.googlecode.funclate.Model;
import com.googlecode.funclate.StringFunclate;
import com.googlecode.lazyrecords.parser.ParametrizedParser;
import com.googlecode.lazyrecords.parser.ParserFunctions;
import com.googlecode.lazyrecords.parser.ParserParameters;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.lazyrecords.parser.StandardParser;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.MediaType;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.handlers.ConvertExtensionToAcceptHeader;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.utterlyidle.modules.ModuleDefiner;
import com.googlecode.utterlyidle.modules.ModuleDefinitions;
import com.googlecode.utterlyidle.modules.RequestScopedModule;
import com.googlecode.utterlyidle.modules.ResourcesModule;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.Containers;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;
import static com.googlecode.utterlyidle.handlers.ConvertExtensionToAcceptHeader.Replacements.replacements;

public class SearchModule implements ParserFunctionsModule, ResourcesModule, ApplicationScopedModule, RequestScopedModule, ModuleDefiner, ParserParametersModule {
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
                add(ParserFunctions.class);
        return Containers.addInstanceIfAbsent(container, ConvertExtensionToAcceptHeader.Replacements.class, replacements(Pair.pair("json", MediaType.APPLICATION_JSON))).
                decorate(HttpHandler.class, ConvertExtensionToAcceptHeader.class);
    }

    public ModuleDefinitions defineModules(ModuleDefinitions moduleDefinitions) throws Exception {
        return moduleDefinitions.addRequestModule(ParserParametersModule.class).addRequestModule(ParserFunctionsModule.class);
    }

    @Override
    public ParserFunctions addFunctions(ParserFunctions parserFunctions, Container container) {
        final Properties properties = container.get(Properties.class);
        return parserFunctions.add("properties", Predicates.always(), StringFunclate.functions.first(new UnaryFunction<String>() {
            @Override
            public String call(String key) throws Exception {
                return properties.getProperty(key);
            }
        }));
    }

    static final Callable2<String, String, String> addHoursFunction = new Callable2<String, String, String>() {
        @Override
        public String call(String s, String s2) throws Exception {
            DateFormat dateFormatter = getDateFormat();
            Date parsedDate = dateFormatter.parse(s);
            return dateFormatter.format(Dates.add(parsedDate, Calendar.HOUR, Integer.parseInt(s2)));
        }
    };

    static DateFormat getDateFormat() {
        return Dates.javaUtilDateToString();
    }

    public ParserFunctions addHoursFunction(ParserFunctions parserFunctions) {
        return parserFunctions.add("addHours", Predicates.always(), StringFunclate.functions.both(addHoursFunction));
    }

    @Override
    public Container addPerApplicationObjects(Container container) throws Exception {
        Containers.addIfAbsent(container, Properties.class);
        return container;
    }

    @Override
    public ParserParameters addParameters(ParserParameters parameters, Container container) {
        return parameters.add("now", container.get(Clock.class).now());
    }
}