package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
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

    public Sequence<Keyword> fields() {
        return allFields;
    }





    public static Sequence<Keyword> uniqueFields(XmlDefinition xmlDefinition) {
        return allFields(xmlDefinition).filter(where(metadata(Keywords.UNIQUE), is(true)));
    }

    public static Sequence<Keyword> allFields(XmlDefinition xmlDefinition) {
        return xmlDefinition.fields().flatMap(allFields());
    }

    public static Callable1<? super Keyword, Sequence<Keyword>> allFields() {
        return new Callable1<Keyword, Sequence<Keyword>>() {
            public Sequence<Keyword> call(Keyword keyword) throws Exception {
                XmlDefinition xmlDefinition = keyword.metadata().get(XmlDefinition.XML_DEFINITION);
                if(xmlDefinition != null){
                    return sequence(keyword).join(allFields(xmlDefinition));
                }
                return sequence(keyword);
            }
        };
    }


}
