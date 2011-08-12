package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class XmlDefinitionExtractor {

    public static XmlDefinition extractFrom(FormParameters form) {
        return extractFrom(form, "");
    }

    private static XmlDefinition extractFrom(FormParameters form, String prefix) {
        Sequence<Triple<Boolean, Keyword, String>> pairs = extractKeywordsFrom(prefix, form);
        Sequence<Keyword> uniqueKeys = uniqueKeys(pairs);
        Sequence<Keyword> allKeys = pairs.map(second(Keyword.class));
        return new XmlDefinition(keyword(form.getValue(prefix + "rootXPath")), allKeys, uniqueKeys);
    }

    private static Sequence<Keyword> uniqueKeys(Sequence<Triple<Boolean, Keyword, String>> pairs) {
        return pairs.filter(where(first(Boolean.class), is(true))).map(second(Keyword.class));
    }

    static Sequence<Triple<Boolean, Keyword, String>> extractKeywordsFrom(String prefix, FormParameters form) {
        Iterable<String> fields = form.getValues(prefix + "fields");
        Iterable<String> aliases = form.getValues(prefix + "aliases");
        Iterable<String> types = form.getValues(prefix + "types");
        Iterable<String> keys = form.getValues(prefix + "unique");
        return toKeywords(fields, aliases, types, new CheckboxValues(keys));
    }

    static Sequence<Triple<Boolean, Keyword, String>> toKeywords(Iterable<String> fields, Iterable<String> aliases, Iterable<String> types, Iterable<Boolean> keys) {
        return Sequences.zip(fields, aliases, types, keys).filter(where(first(String.class), not(empty()))).map(asKeyword()).realise();
    }

    static Callable1<? super Quadruple<String, String, String, Boolean>, Triple<Boolean, Keyword, String>> asKeyword() {
        return new Callable1<Quadruple<String, String, String, Boolean>, Triple<Boolean, Keyword, String>>() {
            public Triple<Boolean, Keyword, String> call(Quadruple<String, String, String, Boolean> quadruple) throws Exception {
                return Triple.triple(quadruple.fourth(), toKeyword(quadruple), extractSubFeed(quadruple.third()));
            }
        };
    }

    static String extractSubFeed(String type) {
        String[] parts = type.split("#");
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    static Keyword toKeyword(Triple<String, String, String> triple) throws ClassNotFoundException {
        String className = triple.third();
        Class aClass = classOf(className);
        ImmutableKeyword source = keyword(triple.first(), aClass);
        String alias = triple.second();
        if (!alias.isEmpty()) {
            return source.as(keyword(alias, aClass));
        }
        return source;
    }

    static Class<?> classOf(String className) throws ClassNotFoundException {
        return Class.forName(className.split("#")[0]);
    }



}
