package com.googlecode.barongreenback.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

public class QueryParserActivator{
    private final Version version;
    private final Analyzer analyzer;

    public QueryParserActivator(final Version version, final Analyzer analyzer) {
        this.version = version;
        this.analyzer = analyzer;
    }

    public MultiFieldQueryParser create(String... fields) {
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(version, fields, analyzer);
        queryParser.setLowercaseExpandedTerms(false);
        queryParser.setAllowLeadingWildcard(true);
        queryParser.setDefaultOperator(QueryParser.Operator.AND);
        return queryParser;
    }
}
