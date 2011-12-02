package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.records.Keywords.metadata;

public class CheckPointStopper implements Feeder<Uri> {
    private final Object currentCheckPoint;
    private final Feeder<Uri> feeder;

    public CheckPointStopper(Object currentCheckPoint, Feeder<Uri> feeder) {
        this.currentCheckPoint = currentCheckPoint;
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).
                takeWhile(not(checkpointReached()));
    }

    public static Option<Object> extractCheckpoint(Record record) {
        return record.keywords().
                find(where(metadata(Crawler.CHECKPOINT), is(true))).
                map(checkpoint(record));
    }

    private Predicate<? super Record> checkpointReached() {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                return extractCheckpoint(record).
                        map(matchesCurrentCheckPoint()).
                        getOrElse(false);
            }
        };
    }

    private Callable1<Object, Boolean> matchesCurrentCheckPoint() {
        return new Callable1<Object, Boolean>() {
            public Boolean call(Object instance) throws Exception {
                return currentCheckPoint != null && currentCheckPoint.equals(instance);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static Callable1<Keyword, Object> checkpoint(final Record record) {
        return new Callable1<Keyword, Object>() {
            public Object call(Keyword keyword) throws Exception {
                return record.get(keyword);
            }
        };
    }

}
