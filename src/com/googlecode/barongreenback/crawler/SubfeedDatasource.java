package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Uri;

public class SubfeedDatasource extends HttpDatasource {
    private final Record record;

    private SubfeedDatasource(Uri uri, Definition source, Record record) {
        super(uri, source);
        this.record = record;
    }

    public static SubfeedDatasource datasource(Uri uri, Definition source, Record record) {
        return new SubfeedDatasource(uri, source, record);
    }

    public Record record() {
        return record;
    }
}
