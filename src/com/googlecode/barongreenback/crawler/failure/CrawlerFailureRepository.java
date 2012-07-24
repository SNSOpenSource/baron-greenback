package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.Repository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Uri;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class CrawlerFailureRepository implements Repository<UUID, Failure> {
    public static final Keyword<UUID> failureId = keyword("failureId", UUID.class);
    public static final Keyword<String> type = keyword("type", String.class);
    public static final Keyword<String> reason = keyword("reason", String.class);
    public static final Keyword<Uri> uri = keyword("uri", Uri.class);
    public static final Keyword<String> source = keyword("source", String.class);
    public static final Keyword<UUID> crawlerId = keyword("crawlerId", UUID.class);
    public static final Keyword<String> record = keyword("record", String.class);

    private static final Definition failures = Definition.constructors.definition("failures", failureId, type, reason, uri, crawlerId, source, record);

    private final Records records;
    private final Container scope;

    public CrawlerFailureRepository(BaronGreenbackRecords records, Container scope) {
        this.scope = scope;
        this.records = records.value();
    }

    @Override
    public void set(UUID id, Failure failure) {
        Record record = FailureMarshallers.forJob(failure.job()).marshaller(scope).marshal(failure).set(failureId, id);
        records.put(failures, update(using(failureId), record));
    }

    @Override
    public Option<Failure> get(UUID id) {
        Record record = records.get(failures).find(where(failureId, is(id))).get();
        return option(FailureMarshallers.valueOf(record.get(type)).marshaller(scope).unmarshal(record));
    }

    @Override
    public void remove(UUID key) {
        throw new UnsupportedOperationException("not done yet");
    }
}
