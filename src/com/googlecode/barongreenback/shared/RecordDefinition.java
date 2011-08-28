package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.AliasedKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

import java.util.List;

import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.MapRecord.record;

public class RecordDefinition {
    public static final Keyword<RecordDefinition> RECORD_DEFINITION = keyword(RecordDefinition.class.getName(), RecordDefinition.class);
    private final Keyword<Object> recordName;
    private final Sequence<Keyword> fields;

    public RecordDefinition(Keyword<Object> recordName, Sequence<Keyword> fields) {
        this.recordName = recordName;
        this.fields = fields;
    }

    public Keyword<Object> recordName() {
        return recordName;
    }

    public Sequence<Keyword> fields() {
        return fields;
    }


    public static Sequence<Keyword> uniqueFields(RecordDefinition recordDefinition) {
        return allFields(recordDefinition).filter(where(metadata(Keywords.UNIQUE), is(true)));
    }

    public static Sequence<Keyword> allFields(RecordDefinition recordDefinition) {
        return recordDefinition.fields().flatMap(allFields());
    }

    public static Callable1<? super Keyword, Sequence<Keyword>> allFields() {
        return new Callable1<Keyword, Sequence<Keyword>>() {
            public Sequence<Keyword> call(Keyword keyword) throws Exception {
                RecordDefinition recordDefinition = keyword.metadata().get(RecordDefinition.RECORD_DEFINITION);
                if (recordDefinition != null) {
                    return sequence(keyword).join(allFields(recordDefinition));
                }
                return sequence(keyword);
            }
        };
    }

    public Model toModel() {
        return toModel(recordName(), fields());
    }

    public static Model toModel(final Keyword<Object> keyword, final Sequence<Keyword> fields) {
        return recordDefinition(keyword.name(),
                fields.map(new Callable1<Keyword, Model>() {
                    public Model call(Keyword keyword) throws Exception {
                        return keywordDefinition(name(keyword), alias(keyword), group(keyword), type(keyword), unique(keyword), visible(keyword), recordDefinition(keyword));
                    }
                }).toArray(Model.class));
    }

    public static Option<Model> recordDefinition(Keyword keyword) {
        return option(keyword.metadata().get(RecordDefinition.RECORD_DEFINITION)).map(new Callable1<RecordDefinition, Model>() {
            public Model call(RecordDefinition recordDefinition) throws Exception {
                return recordDefinition.toModel();
            }
        });
    }

    private static boolean visible(Keyword keyword) {
        return booleanValueOf(keyword, Views.VISIBLE);
    }

    private static boolean booleanValueOf(Keyword keyword, Keyword<Boolean> metaKeyword) {
        try {
            return true == keyword.metadata().get(metaKeyword);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean unique(Keyword keyword) {
        return booleanValueOf(keyword, Keywords.UNIQUE);
    }

    private static String name(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return ((AliasedKeyword) keyword).source().name();
        }
        return keyword.name();
    }

    private static String alias(Keyword keyword) {
        if (keyword instanceof AliasedKeyword) {
            return keyword.name();
        }
        return "";
    }

    private static String group(Keyword keyword) {
        return keyword.metadata().get(Views.GROUP);
    }

    private static String type(Keyword keyword) {
        return keyword.forClass().getName();
    }

    public static Model recordDefinition(String recordName, Model... fields) {
        return model().add("name", recordName).add("keywords", Sequences.sequence(fields).toList());
    }

    public static Model keywordDefinition(String name, String alias, String group, String type, boolean unique, boolean visible, Option<Model> recordDefinition) {
        return model().
                add("name", name).
                add("alias", alias).
                add("group", group).
                add("type", type).
                add("unique", unique).
                add("visible", visible).
                add("subfeed", !recordDefinition.isEmpty()).
                add("record", recordDefinition.getOrNull());
    }

    public static RecordDefinition convert(Model model) {
        if (model == null) {
            return null;
        }
        return new RecordDefinition(keyword(model.get("name", String.class)), toKeywords(model.getValues("keywords", Model.class)));
    }

    private static Sequence<Keyword> toKeywords(List<Model> keywords) {
        return sequence(keywords).map(new Callable1<Model, Keyword>() {
            public Keyword call(Model model) throws Exception {
                return keyword(
                        model.get("name", String.class),
                        Class.forName(model.get("type", String.class))).
//                        as((Keyword) keyword(model.get("alias", String.class))).
        metadata(record().
        set(Keywords.UNIQUE, model.get("unique", Boolean.class)).
        set(Views.VISIBLE, model.get("visible", Boolean.class)).
        set(Views.GROUP, model.get("group", String.class)).
        set(RecordDefinition.RECORD_DEFINITION, convert(model.get("record", Model.class))));
            }
        });
    }


}
