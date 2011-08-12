package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;

public class XmlDefinition {
    private final Keyword<Object> rootXPath;
    private final Sequence<Keyword> allFields;
    private final Sequence<Keyword> uniqueFields;

    public XmlDefinition(Keyword<Object> rootXPath, Sequence<Keyword> allFields, Sequence<Keyword> uniqueFields) {
        this.rootXPath = rootXPath;
        this.allFields = allFields;
        this.uniqueFields = uniqueFields;
    }

    public Keyword<Object> rootXPath() {
        return rootXPath;
    }

    public Sequence<Keyword> allFields() {
        return allFields;
    }

    public Sequence<Keyword> uniqueFields() {
        return uniqueFields;
    }
}
