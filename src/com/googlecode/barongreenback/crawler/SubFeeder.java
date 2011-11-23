package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.RecordMethods.merge;

public class SubFeeder implements Feeder<Uri> {
    private final Feeder<Uri> feeder;

    public SubFeeder(Feeder<Uri> feeder) {
        this.feeder = feeder;
    }

    public Sequence<Record> get(final Uri source, RecordDefinition definition) throws Exception {
        return feeder.get(source, definition).
                flatMap(allSubFeeds());
    }

    private Callable1<Record, Sequence<Record>> allSubFeeds() {
        return new Callable1<Record, Sequence<Record>>() {
            public Sequence<Record> call(final Record record) throws Exception {
                Sequence<Keyword> subFeedKeys = record.keywords().filter(where(metadata(RECORD_DEFINITION), is(notNullValue())));
                if (subFeedKeys.isEmpty()) {
                    return one(record);
                }
                return subFeedKeys.flatMap(eachSubFeedWith(record));
            }
        };
    }

    private Callable1<Keyword, Sequence<Record>> eachSubFeedWith(final Record record) {
        return new Callable1<Keyword, Sequence<Record>>() {
            public Sequence<Record> call(Keyword keyword) throws Exception {
                Uri subFeed = uri(record.get(keyword).toString());
                RecordDefinition subFeedDefinition = keyword.metadata().get(RECORD_DEFINITION);
                return get(subFeed, subFeedDefinition).
                        map(merge(record));
            }
        };
    }
}
