package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.time.Dates;

import java.util.Date;

import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;

public class CheckpointStopper implements Feeder<Uri> {
    private final Object currentCheckpoint;
    private final Feeder<Uri> feeder;

    public CheckpointStopper(Object currentCheckpoint, Feeder<Uri> feeder) {
        this.currentCheckpoint = currentCheckpoint;
        this.feeder = feeder;
    }

    public static Sequence<Record> stopAt(Object checkpoint, Sequence<Record> records) {
        return records.takeWhile(not(CheckpointStopper.checkpointReached(checkpoint))).realise();
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).
                takeWhile(not(checkpointReached(currentCheckpoint)));
    }

    public static Option<Object> extractCheckpoint(Record record) {
        return record.keywords().
                find(where(metadata(CompositeCrawler.CHECKPOINT), is(true))).
                map(checkpoint(record));
    }

    public static Predicate<Record> checkpointReached(final Object currentCheckpoint) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                return extractCheckpoint(record).
                        map(matchesCurrentCheckpoint(currentCheckpoint)).
                        getOrElse(false);
            }
        };
    }

    public static LogicalPredicate<Object> matchesCurrentCheckpoint(final Object currentCheckpoint) {
        return new LogicalPredicate<Object>() {
            @Override
            public boolean matches(Object instance) {
                if (currentCheckpoint == null) {
                    return false;
                }
                if (currentCheckpoint instanceof Date) {
                    Date updatedDate = Dates.parse(instance.toString());
                    Date checkpointDate = (Date) currentCheckpoint;
                    return updatedDate.before(checkpointDate) || (updatedDate.equals(checkpointDate));
                }
                return currentCheckpoint.equals(instance);
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