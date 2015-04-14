package com.googlecode.barongreenback.search;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.lazyrecords.mappings.DateMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.parser.PredicateParser;
import com.googlecode.lazyrecords.parser.StandardParser;

import java.util.Date;
import java.util.concurrent.Callable;

public class StandardParserActivator implements Callable<PredicateParser> {
    private final StringMappings stringMappings;

    public StandardParserActivator(ParserUkDateConverter dateConverter, BaronGreenbackStringMappings mappings) {
        stringMappings = mappings.value().add(Date.class, new DateMapping(dateConverter));
    }

    @Override
    public PredicateParser call() throws Exception {
        return new StandardParser(stringMappings);
    }
}
