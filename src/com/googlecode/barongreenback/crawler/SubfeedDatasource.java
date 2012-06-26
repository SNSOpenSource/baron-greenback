package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

public class SubfeedDatasource extends HttpDataSource {
    private final Sequence<Pair<Keyword<?>, Object>> uniqueIdentifiers;

    private SubfeedDatasource(Uri uri, Definition source, Sequence<Pair<Keyword<?>, Object>> uniqueIdentifiers) {
        super(uri, source);
        this.uniqueIdentifiers = uniqueIdentifiers;
    }

    public static SubfeedDatasource dataSource(Uri uri, Definition source, Sequence<Pair<Keyword<?>, Object>> uniqueIdentifiers) {
        return new SubfeedDatasource(uri, source, uniqueIdentifiers);
    }

    public Sequence<Pair<Keyword<?>, Object>> uniqueIdentifiers() {
        return uniqueIdentifiers;
    }
}
