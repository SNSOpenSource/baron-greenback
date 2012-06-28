package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Predicates.*;

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
            Sequence<Keyword<?>> unique = uniqueFields(destination);
            if(newRecords.head().fields().map(Callables.<Keyword<?>>first()).exists(in(unique))) {
                return records.put(destination, Record.methods.update(using(unique), newRecords));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return 0;
    }

    public static Function1<Sequence<Record>, Number> write(final Application application, final StagedJob<Response> job) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(final Sequence<Record> newData) throws Exception {
                return application.usingRequestScope(new Callable1<Container, Number>() {
                    @Override
                    public Number call(Container container) throws Exception {
                        try {
                            Number updated = new DataWriter(container.get(BaronGreenbackRecords.class).value()).writeUnique(job.destination(), newData);
                            job.container().get(AtomicInteger.class).addAndGet(updated.intValue());
                            return updated;
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
