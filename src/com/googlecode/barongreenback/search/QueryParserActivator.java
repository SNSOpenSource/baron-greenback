package com.googlecode.barongreenback.search;

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

import static com.googlecode.barongreenback.search.IndexWriterActivator.analyzer;

public class QueryParserActivator {
    private final Version version;

    public QueryParserActivator(final Version version) {
        this.version = version;
    }

    public MultiFieldQueryParser create(String... fields) {
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(version, fields, analyzer());
        queryParser.setLowercaseExpandedTerms(false);
        queryParser.setAllowLeadingWildcard(true);
        queryParser.setDefaultOperator(QueryParser.Operator.AND);
        return queryParser;
    }
}
