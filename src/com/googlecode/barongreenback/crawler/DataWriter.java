package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

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

    public Number writeUnique(Definition destination, Sequence<Record> newRecords) {
        Sequence<Keyword<?>> unique = destination.fields().filter(UNIQUE_FILTER);
        return records.put(destination, Record.methods.update(using(unique), newRecords));
    }

    public Function1<Sequence<Record>, Number> writeUnique(final Definition destination) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(Sequence<Record> records) throws Exception {
                return writeUnique(destination, records);
            }
        };
    }
}
