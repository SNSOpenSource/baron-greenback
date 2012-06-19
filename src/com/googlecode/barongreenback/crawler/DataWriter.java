package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;

import java.util.Date;

import static com.googlecode.barongreenback.shared.RecordDefinition.UNIQUE_FILTER;
import static com.googlecode.lazyrecords.Using.using;

public class DataWriter {
    private final Records records;

    public DataWriter(BaronGreenbackRecords records) {
        this(records.value());
    }

    public DataWriter(Records records) {
        this.records = records;
    }

    public Number writeUnique(final Definition destination, final Sequence<Record> newRecords) {
        if (newRecords.isEmpty()) return 0;

        try {
            Sequence<Keyword<?>> unique = destination.fields().filter(UNIQUE_FILTER);
            return records.put(destination, Record.methods.update(using(unique), newRecords));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
