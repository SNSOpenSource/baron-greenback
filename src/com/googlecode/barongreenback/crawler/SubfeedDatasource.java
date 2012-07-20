package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Uri;

import java.util.UUID;

public class SubfeedDatasource extends HttpDatasource {
    private final Record record;

    private SubfeedDatasource(Uri uri, UUID id, Definition source, Record record) {
        super(uri, id, source);
        this.record = record;
    }

    public static SubfeedDatasource datasource(Uri uri, UUID id, Definition source, Record record) {
        return new SubfeedDatasource(uri, id, source, record);
    }

    public Record record() {
        return record;
    }
}
