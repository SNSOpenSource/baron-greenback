package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Predicates.not;

public class CheckPointStopper2 {
    public static Function1<Sequence<Record>, Sequence<Record>> stopAt(final Object checkpoint) {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                return stopAt(checkpoint, records);
            }
        };
    }

    public static Sequence<Record> stopAt(Object checkpoint, Sequence<Record> records) {
        return records.takeWhile(not(CheckPointStopper.checkpointReached(checkpoint))).realise();
    }
}
