package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

public class SubfeedDatasource extends HttpDatasource {
    private final Sequence<Pair<Keyword<?>, Object>> data;

    private SubfeedDatasource(Uri uri, Definition source, Sequence<Pair<Keyword<?>, Object>> data) {
        super(uri, source);
        this.data = data;
    }

    public static SubfeedDatasource datasource(Uri uri, Definition source, Sequence<Pair<Keyword<?>, Object>> data) {
        return new SubfeedDatasource(uri, source, data);
    }

    public Sequence<Pair<Keyword<?>, Object>> data() {
        return data;
    }
}
