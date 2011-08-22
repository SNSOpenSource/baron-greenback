package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.views.Views;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.utterlyidle.FormParameters;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.transpose;
import static com.googlecode.totallylazy.Sequences.zip;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class RecordDefinitionExtractor {
    public static final String FIELDS = "fields";
    public static final String ALIASES = "aliases";
    public static final String TYPES = "types";
    public static final String UNIQUE = "unique";
    public static final String VISIBLE = "visible";
    public static final String RECORD_NAME = "recordName";
    public static final String SUB_FEED_DELIMITER = "#";

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
        Iterable<String> fields = form.getValues(prefix + FIELDS);
        Iterable<String> aliases = form.getValues(prefix + ALIASES);
        Iterable<String> types = form.getValues(prefix + TYPES);
        Iterable<Boolean> unique = new CheckboxValues(form.getValues(prefix + UNIQUE));
        Iterable<Boolean> visible = new CheckboxValues(form.getValues(prefix + VISIBLE));
        return toKeywords(fields, aliases, types, unique, visible);
    }

    private Sequence<Keyword> toKeywords(Iterable fields, Iterable aliases, Iterable types, Iterable unique, Iterable visible) {
        Sequence transpose = Sequences.transpose(fields, aliases, types, unique, visible);
        Sequence filter = transpose.
                filter(where(first(String.class), not(empty())));
        return filter.
                map(asKeyword()).
                realise();
    }

    private Callable1<? super Sequence, Keyword> asKeyword() {
        return new Callable1<Sequence, Keyword>() {
            public Keyword call(Sequence sequence) throws Exception {
                return toKeyword((String) sequence.first(), (String) sequence.second(), (String) sequence.drop(2).head(), (Boolean) sequence.drop(3).head(), (Boolean) sequence.drop(4).head());
            }
        };
    }

    private Keyword toKeyword(String name, String alias, String type, Boolean unique, Boolean visible) throws ClassNotFoundException {
        ImmutableKeyword keyword = keyword(name, classOf(type));

        setMetadata(keyword, type, unique, visible);

        if (!alias.isEmpty()) {
            return keyword.as(keyword(alias, classOf(type)));
        }
        return keyword;
    }

    private void setMetadata(ImmutableKeyword keyword, String type, Boolean unique, Boolean visible) {
        keyword.metadata().set(Keywords.UNIQUE, unique);
        keyword.metadata().set(Views.VISIBLE, visible);

        String prefix = extractSubFeed(type);
        if(!prefix.isEmpty()){
            keyword.metadata().set(RecordDefinition.RECORD_DEFINITION, extractWith(prefix));
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
