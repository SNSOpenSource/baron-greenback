package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.search.parser.ParserParameters;
import com.googlecode.utterlyidle.modules.Module;

public interface ParserParametersModule extends Module {
    ParserParameters addParameters(ParserParameters parameters);
}
