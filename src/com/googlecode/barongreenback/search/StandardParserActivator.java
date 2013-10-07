package com.googlecode.barongreenback.search;

import com.googlecode.lazyrecords.parser.ParserDateConverter;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.lazyrecords.parser.StandardParser;

import java.util.concurrent.Callable;

public class StandardParserActivator implements Callable<PredicateParser> {
    private final ParserDateConverter parserDateConverter;

    public StandardParserActivator(ParserDateConverter parserDateConverter) {
        this.parserDateConverter = parserDateConverter;
    }

    @Override
    public PredicateParser call() throws Exception {
        return new StandardParser(parserDateConverter);
    }
}
