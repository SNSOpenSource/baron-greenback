package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.yadic.Container;

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

    public static Function1<Sequence<Record>, Number> write(final Application application, final Definition destination) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(final Sequence<Record> newData) throws Exception {
                return application.usingRequestScope(new Callable1<Container, Number>() {
                    @Override
                    public Number call(Container container) throws Exception {
                        try {
                            return new DataWriter(container.get(BaronGreenbackRecords.class).value()).writeUnique(destination, newData);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        };
    }
}
