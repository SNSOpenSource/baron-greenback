package com.googlecode.barongreenback.search;

import com.googlecode.lazyrecords.parser.ParserFunctions;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.yadic.Container;

public interface ParserFunctionsModule extends Module {
    ParserFunctions addFunctions(ParserFunctions parserFunctions, Container container);
}
