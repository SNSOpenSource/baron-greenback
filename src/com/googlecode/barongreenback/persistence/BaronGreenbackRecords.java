package com.googlecode.barongreenback.persistence;

import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Value;

import java.io.Closeable;
import java.io.IOException;

public class BaronGreenbackRecords implements Value<Records>, Closeable {
    private final Records records;

    private BaronGreenbackRecords(Records records) {
        this.records = records;
    }

    public static BaronGreenbackRecords records(Records records) {
        return new BaronGreenbackRecords(records);
    }

    @Override
    public Records value() {
        return records;
    }

    @Override
    public void close() throws IOException {
        Closeables.close(records);
    }
}
