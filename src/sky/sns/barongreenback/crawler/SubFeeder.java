package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Keyword.functions.metadata;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Uri.uri;
import static java.lang.String.format;

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
                Sequence<Keyword<?>> subFeedKeys = record.keywords().
                        filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                        realise(); // Must Realise so we don't get concurrent modification as we add new fields to the record
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
                Object value = record.get(keyword);
                if (value == null) {
                    return one(record);
                }
                Uri subFeed = uri(value.toString());
                try {
                    RecordDefinition subFeedDefinition = keyword.metadata().get(RECORD_DEFINITION);
                    Sequence<Record> records = get(subFeed, subFeedDefinition).memorise();
                    if (records.isEmpty()) {
                        return one(record);
                    }
                    return records.
                            map(mergeWith(record));
                } catch (LazyException e) {
                    return handleError(subFeed, e.getCause(), record);
                } catch (Exception e) {
                    return handleError(subFeed, e, record);
                }
            }
        };
    }

    private Function1<Record, Record> mergeWith(final Record parentRecord) {
        return new Function1<Record, Record>() {
            @Override
            public Record call(Record subFeedRecord) throws Exception {
                return copyMissingFieldsFromParent(parentRecord, subFeedRecord);
            }
        };
    }

    static Record copyMissingFieldsFromParent(Record parentRecord, Record subFeedRecord) {
        for (Keyword<Object> keyword : parentRecord.keywords().<Keyword<Object>>unsafeCast()) {
            if (!subFeedRecord.keywords().contains(keyword)) {
                subFeedRecord = subFeedRecord.set(keyword, parentRecord.get(keyword));
            }
        }
        return subFeedRecord;
    }

    private Sequence<Record> handleError(Uri subFeed, Throwable e, Record record) {
        System.err.println(format("Failed to GET %s because of %s", subFeed, e));
        return one(record);
    }
}
