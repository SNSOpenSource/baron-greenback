package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import java.util.Date;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.metadata;

public class CheckPointStopper implements Feeder<Uri> {
    private final Date date;
    private final Feeder<Uri> feeder;

    public CheckPointStopper(Date date, Feeder<Uri> feeder) {
        this.date = date;
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).takeWhile(not(checkpointReached(date)));
    }

    private Predicate<? super Record> checkpointReached(final Date checkpointValue) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                Option<Keyword> checkpoint = record.keywords().find(where(metadata(Crawler.CHECKPOINT), is(true)));
                if (!checkpoint.isEmpty() && checkpointValue != null) {
                    Date recordDate = (Date) record.get(checkpoint.get());
                    return recordDate.equals(checkpointValue);
                }
                return false;
            }
        };
    }

}
