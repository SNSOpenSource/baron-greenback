package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.Finder;
import com.googlecode.barongreenback.shared.Repository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class FailureRepository implements Repository<UUID, Record>, Finder<Record> {
    public static final Keyword<UUID> ID = keyword("id", UUID.class);
    public static final Keyword<String> JOB_TYPE = keyword("jobType", String.class);
    public static final Keyword<String> REASON = keyword("reason", String.class);
    public static final Keyword<Uri> URI = keyword("uri", Uri.class);
    public static final Keyword<String> SOURCE = keyword("source", String.class);
    public static final Keyword<UUID> CRAWLER_ID = keyword("crawlerId", UUID.class);
    public static final Keyword<String> RECORD = keyword("record", String.class);
    public static final Keyword<String> VISITED = keyword("visited", String.class);

    private static final Definition FAILURES = Definition.constructors.definition("failures", ID, JOB_TYPE, REASON, URI, CRAWLER_ID, SOURCE, RECORD, VISITED);

    private final Records records;

    public FailureRepository(BaronGreenbackRecords records) {
        this.records = records.value();
    }

    @Override
    public void set(UUID id, Record record) {
        records.put(FAILURES, update(using(ID), record.set(ID, id)));
    }

    @Override
    public Option<Record> get(UUID id) {
        return records.get(FAILURES).find(where(ID, is(id)));
    }

    @Override
    public void remove(UUID id) {
        records.remove(FAILURES, where(ID, is(id)));
    }

    @Override
    public Sequence<Record> find(Predicate<? super Record> predicate) {
        return records.get(FAILURES).filter(predicate);
    }

    public int size() {
        return records.get(FAILURES).size();
    }

    public boolean isEmpty() {
        return records.get(FAILURES).isEmpty();
    }

    public int removeAll() {
        return records.remove(FAILURES).intValue();
    }
}
