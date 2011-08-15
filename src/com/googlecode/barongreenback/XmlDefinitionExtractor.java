package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.zip;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class XmlDefinitionExtractor {
    public static final String FIELDS = "fields";
    public static final String ALIASES = "aliases";
    public static final String TYPES = "types";
    public static final String UNIQUE = "unique";
    public static final String ROOT_XPATH = "rootXPath";
    public static final String SUB_FEED_DELIMITER = "#";
    public static final Keyword<XmlDefinition> XML_DEFINITION = keyword(XmlDefinition.class.getName(), XmlDefinition.class);

    private final FormParameters form;

    public XmlDefinitionExtractor(FormParameters form) {
        this.form = form;
    }

    public XmlDefinition extract() {
        return extractWith("");
    }

    private XmlDefinition extractWith(String prefix) {
        Sequence<Keyword> keywords = extractKeywordsWith(prefix);
        return new XmlDefinition(keyword(form.getValue(prefix + ROOT_XPATH)), keywords);
    }

    private Sequence<Keyword> extractKeywordsWith(String prefix) {
        Iterable<String> fields = form.getValues(prefix + FIELDS);
        Iterable<String> aliases = form.getValues(prefix + ALIASES);
        Iterable<String> types = form.getValues(prefix + TYPES);
        Iterable<Boolean> unique = new CheckboxValues(form.getValues(prefix + UNIQUE));
        return toKeywords(fields, aliases, types, unique);
    }

    private Sequence<Keyword> toKeywords(Iterable<String> fields, Iterable<String> aliases, Iterable<String> types, Iterable<Boolean> unique) {
        return zip(fields, aliases, types, unique).
                filter(where(first(String.class), not(empty()))).
                map(asKeyword()).
                realise();
    }

    private Callable1<? super Quadruple<String, String, String, Boolean>, Keyword> asKeyword() {
        return new Callable1<Quadruple<String, String, String, Boolean>, Keyword>() {
            public Keyword call(Quadruple<String, String, String, Boolean> quadruple) throws Exception {
                return toKeyword(quadruple.first(), quadruple.second(), quadruple.third(), quadruple.fourth());
            }
        };
    }

    private Keyword toKeyword(String name, String alias, String type, Boolean unique) throws ClassNotFoundException {
        ImmutableKeyword keyword = keyword(name, classOf(type));

        setMetadata(keyword, type, unique);

        if (!alias.isEmpty()) {
            return keyword.as(keyword(alias, classOf(type)));
        }
        return keyword;
    }

    private void setMetadata(ImmutableKeyword keyword, String type, Boolean unique) {
        keyword.metadata().set(Keywords.UNIQUE, unique);

        String prefix = extractSubFeed(type);
        if(!prefix.isEmpty()){
            keyword.metadata().set(XML_DEFINITION, extractWith(prefix));
        }
    }

    private String extractSubFeed(String type) {
        String[] parts = type.split(SUB_FEED_DELIMITER);
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private Class<?> classOf(String className) throws ClassNotFoundException {
        return Class.forName(className.split(SUB_FEED_DELIMITER)[0]);
    }



}
