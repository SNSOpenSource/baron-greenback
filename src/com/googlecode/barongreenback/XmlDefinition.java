package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.metadata;

public class XmlDefinition {
    public static final Keyword<XmlDefinition> XML_DEFINITION = keyword(XmlDefinition.class.getName(), XmlDefinition.class);
    private final Keyword<Object> rootXPath;
    private final Sequence<Keyword> allFields;

    public XmlDefinition(Keyword<Object> rootXPath, Sequence<Keyword> allFields) {
        this.rootXPath = rootXPath;
        this.allFields = allFields;
    }

    public Keyword<Object> rootXPath() {
        return rootXPath;
    }

    public Sequence<Keyword> allFields() {
        return allFields;
    }

    public Sequence<Keyword> uniqueFields() {
        return allFields.filter(where(metadata(Keywords.UNIQUE), is(true)));
    }

}
