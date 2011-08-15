package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class XmlDefinitionExtractor {
    public static final String FIELDS = "fields";
    public static final String ALIASES = "aliases";
    public static final String TYPES = "types";
    public static final String UNIQUE = "unique";
    public static final String ROOT_XPATH = "rootXPath";
    public static final String SUB_FEED_DELIMITER = "#";

    private final FormParameters form;

    public XmlDefinitionExtractor(FormParameters form) {
        this.form = form;
    }

    public XmlDefinition extract() {
        return extractWith("");
    }

    private XmlDefinition extractWith(String prefix) {
        Sequence<Pair<Keyword, String>> pairs = extractKeywordsWith(prefix);
        Sequence<Keyword> allKeys = pairs.map(first(Keyword.class));
        return new XmlDefinition(keyword(form.getValue(prefix + ROOT_XPATH)), allKeys);
    }

    private Sequence<Pair<Keyword, String>> extractKeywordsWith(String prefix) {
        Iterable<String> fields = form.getValues(prefix + FIELDS);
        Iterable<String> aliases = form.getValues(prefix + ALIASES);
        Iterable<String> types = form.getValues(prefix + TYPES);
        Iterable<String> keys = form.getValues(prefix + UNIQUE);
        return toKeywords(fields, aliases, types, new CheckboxValues(keys));
    }

    private Sequence<Pair<Keyword, String>> toKeywords(Iterable<String> fields, Iterable<String> aliases, Iterable<String> types, Iterable<Boolean> unique) {
        return Sequences.zip(fields, aliases, types, unique).filter(where(first(String.class), not(empty()))).map(asKeyword()).realise();
    }

    private Callable1<? super Quadruple<String, String, String, Boolean>, Pair<Keyword, String>> asKeyword() {
        return new Callable1<Quadruple<String, String, String, Boolean>, Pair<Keyword, String>>() {
            public Pair<Keyword, String> call(Quadruple<String, String, String, Boolean> quadruple) throws Exception {
                return Pair.pair(toKeyword(quadruple), extractSubFeed(quadruple.third()));
            }
        };
    }

    private String extractSubFeed(String type) {
        String[] parts = type.split(SUB_FEED_DELIMITER);
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private Keyword toKeyword(Quadruple<String, String, String, Boolean> quadruple) throws ClassNotFoundException {
        String className = quadruple.third();
        Class aClass = classOf(className);
        ImmutableKeyword source = keyword(quadruple.first(), aClass);
        source.metadata().set(Keywords.UNIQUE, quadruple.fourth());
        String alias = quadruple.second();
        if (!alias.isEmpty()) {
            return source.as(keyword(alias, aClass));
        }
        return source;
    }

    private Class<?> classOf(String className) throws ClassNotFoundException {
        return Class.forName(className.split(SUB_FEED_DELIMITER)[0]);
    }



}
