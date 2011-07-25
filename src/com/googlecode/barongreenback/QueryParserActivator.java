package com.googlecode.barongreenback;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

import java.util.concurrent.Callable;

public class QueryParserActivator implements Callable<QueryParser>{
    private final Version version;
    private final Analyzer analyzer;

    public QueryParserActivator(final Version version, final Analyzer analyzer) {
        this.version = version;
        this.analyzer = analyzer;
    }

    public QueryParser call() throws Exception {
        return new QueryParser(version, "", analyzer);
    }
}
