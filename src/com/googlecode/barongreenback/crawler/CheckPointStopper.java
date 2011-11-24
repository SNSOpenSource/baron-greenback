package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import static com.googlecode.totallylazy.Predicates.all;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.metadata;

public class CheckPointStopper implements Feeder<Uri> {
    private final Object currentCheckPoint;
    private final Feeder<Uri> feeder;

    public CheckPointStopper(Object currentCheckPoint, Feeder<Uri> feeder) {
        this.currentCheckPoint = currentCheckPoint;
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).takeWhile(not(checkpointReached()));
    }

    private Predicate<? super Record> checkpointReached() {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                return record.keywords().
                        find(where(metadata(Crawler.CHECKPOINT), is(true))).
                        map(matchesCurrentCheckpoint(record)).
                        getOrElse(false);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Callable1<Keyword, Boolean> matchesCurrentCheckpoint(final Record record) {
        return new Callable1<Keyword, Boolean>() {
            public Boolean call(Keyword keyword) throws Exception {
                return currentCheckPoint.equals(record.get(keyword));
            }
        };
    }

}
