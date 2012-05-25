package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;

public class CheckPointStopper implements Feeder<Uri> {
    private final Object currentCheckPoint;
    private final Feeder<Uri> feeder;

    public CheckPointStopper(Object currentCheckPoint, Feeder<Uri> feeder) {
        this.currentCheckPoint = currentCheckPoint;
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).
                takeWhile(not(checkpointReached(currentCheckPoint)));
    }

    public static Option<Object> extractCheckpoint(Record record) {
        return record.keywords().
                find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).
                map(checkpoint(record));
    }

    public static Predicate<? super Record> checkpointReached(final Object currentCheckPoint) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                return extractCheckpoint(record).
                        map(matchesCurrentCheckPoint(currentCheckPoint)).
                        getOrElse(false);
            }
        };
    }

    public static  Callable1<Object, Boolean> matchesCurrentCheckPoint(final Object currentCheckPoint) {
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

    public static Callable1<Record, Object> extractCheckPoint() {
        return new Callable1<Record, Object>() {
            @Override
            public Object call(Record record) throws Exception {
                return extractCheckpoint(record);
            }
        };
    }
}
