package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.views.Views;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.transpose;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class RecordDefinitionExtractor {
    public static final String KEYWORD_NAME = "keywordName";
    public static final String ALIASES = "aliases";
    public static final String GROUP = "group";
    public static final String TYPES = "types";
    public static final String UNIQUE = "unique";
    public static final String VISIBLE = "visible";
    public static final String SUBFEED = "subfeed";
    public static final String SUBFEED_PREFIX = "subfeedPrefix";

    public static final String RECORD_NAME = "recordName";
    private final FormParameters form;

    public RecordDefinitionExtractor(FormParameters form) {
        this.form = form;
    }

    public RecordDefinition extract() {
        return extractWith("");
    }

    private RecordDefinition extractWith(String prefix) {
        Sequence<Keyword> keywords = extractKeywordsWith(prefix);
        return new RecordDefinition(keyword(form.getValue(prefix + RECORD_NAME)), keywords);
    }

    private Sequence<Keyword> extractKeywordsWith(String prefix) {
        Iterable<String> name = form.getValues(prefix + KEYWORD_NAME);
        Iterable<String> aliases = form.getValues(prefix + ALIASES);
        Iterable<String> groups = form.getValues(prefix + GROUP);
        Iterable<String> types = form.getValues(prefix + TYPES);
        Iterable<Boolean> unique = new CheckboxValues(form.getValues(prefix + UNIQUE));
        Iterable<Boolean> visible = new CheckboxValues(form.getValues(prefix + VISIBLE));
        Iterable<Boolean> subfeed = new CheckboxValues(form.getValues(prefix + SUBFEED));
        Iterable<String> subfeedPrefix = form.getValues(prefix + SUBFEED_PREFIX);
        return toKeywords(name, aliases, groups, types, unique, visible, subfeed, subfeedPrefix);
    }

    private Sequence<Keyword> toKeywords(Iterable name, Iterable aliases, Iterable group, Iterable types, Iterable unique, Iterable visible, Iterable subfeed, Iterable subfeedPrefix) {
       return transpose(name, aliases, group, types, unique, visible, subfeed, subfeedPrefix).
                filter(where(first(String.class), not(empty()))).
                map(asKeyword()).
                realise();
    }

    private Callable1<? super Sequence, Keyword> asKeyword() {
        return new Callable1<Sequence, Keyword>() {
            public Keyword call(Sequence sequence) throws Exception {
                return toKeyword((String) sequence.first(), (String) sequence.second(), (String) sequence.drop(2).head(),
                                 (String) sequence.drop(3).head(), (Boolean) sequence.drop(4).head(), (Boolean) sequence.drop(5).head(),
                                 (Boolean) sequence.drop(6).head(), (String) sequence.drop(7).head());
            }
        };
    }

    private Keyword toKeyword(String name, String alias, String group, String type, Boolean unique, Boolean visible, Boolean subfeed, String subfeedPrefix) throws ClassNotFoundException {
        ImmutableKeyword keyword = keyword(name, classOf(type));

        setMetadata(keyword, type, group, unique, visible, subfeed, subfeedPrefix);

        if (!alias.isEmpty()) {
            return keyword.as(keyword(alias, classOf(type)));
        }
        return keyword;
    }

    private void setMetadata(ImmutableKeyword keyword, String type, String group, Boolean unique, Boolean visible, Boolean subfeed, String subfeedPrefix) {
        keyword.metadata().set(Keywords.UNIQUE, unique);
        keyword.metadata().set(Views.VISIBLE, visible);
        keyword.metadata().set(Views.GROUP, group);

        if(subfeed){
            keyword.metadata().set(RecordDefinition.RECORD_DEFINITION, extractWith(subfeedPrefix));
        }
    }

    private Class<?> classOf(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }



}
