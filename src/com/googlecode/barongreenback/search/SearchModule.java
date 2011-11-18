package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.search.pager.Pager;
import com.googlecode.barongreenback.search.parser.ParametrizedParser;
import com.googlecode.barongreenback.search.parser.ParserParameters;
import com.googlecode.barongreenback.search.parser.PredicateParser;
import com.googlecode.barongreenback.search.parser.StandardParser;
import com.googlecode.utterlyidle.Resources;
import com.googlecode.utterlyidle.modules.*;
import com.googlecode.yadic.Container;
import org.apache.lucene.queryParser.ParseException;

import static com.googlecode.utterlyidle.annotations.AnnotatedBindings.annotatedClass;

public class SearchModule implements ResourcesModule, RequestScopedModule, ModuleDefiner {
    public Module addResources(Resources resources) throws ParseException {
        resources.add(annotatedClass(SearchResource.class));
        return this;
    }

    public Module addPerRequestObjects(Container container) throws Exception {
		container.add(Pager.class);
        container.add(PredicateParser.class, StandardParser.class);
        container.decorate(PredicateParser.class, ParametrizedParser.class);
        container.add(ParserParameters.class);
        return this;
    }

    public Module defineModules(ModuleDefinitions moduleDefinitions) throws Exception {
        moduleDefinitions.addRequestModule(ParserParametersModule.class);
        return this;
    }
}
