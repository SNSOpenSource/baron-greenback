package com.googlecode.barongreenback.views;

import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;

public class Views {
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = Keywords.keyword("group", String.class);
}
