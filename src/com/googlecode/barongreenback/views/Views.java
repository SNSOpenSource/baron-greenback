package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.funclate.Model.model;

public class Views {
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = Keywords.keyword("group", String.class);

    public static Model clean(Model root) {
        return view(convert(root.get("view", Model.class)).toModel());
    }

    public static Model view(Keyword<Object> recordName, Sequence<Keyword> keywords) {
        return view(new RecordDefinition(recordName, keywords));
    }

    public static Model view(RecordDefinition recordDefinition) {
        return view(recordDefinition.toModel());
    }

    public static Model view(Model definition) {
        return model().add("view", definition);
    }
}
