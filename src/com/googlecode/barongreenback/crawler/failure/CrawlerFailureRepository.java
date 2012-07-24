package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.Finder;
import com.googlecode.barongreenback.shared.Repository;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class CrawlerFailureRepository implements Repository<UUID, Failure>, Finder<Pair<UUID, Failure>> {
    public static final Keyword<UUID> ID = keyword("id", UUID.class);
    public static final Keyword<String> JOB_TYPE = keyword("jobType", String.class);
    public static final Keyword<String> REASON = keyword("reason", String.class);
    public static final Keyword<Uri> URI = keyword("uri", Uri.class);
    public static final Keyword<String> SOURCE = keyword("source", String.class);
    public static final Keyword<UUID> CRAWLER_ID = keyword("crawlerId", UUID.class);
    public static final Keyword<String> RECORD = keyword("record", String.class);

    private static final Definition FAILURES = Definition.constructors.definition("failures", ID, JOB_TYPE, REASON, URI, CRAWLER_ID, SOURCE, RECORD);

    private final Records records;
    private final Container scope;

    public CrawlerFailureRepository(BaronGreenbackRecords records, Container scope) {
        this.scope = scope;
        this.records = records.value();
    }

    @Override
    public void set(UUID id, Failure failure) {
        Record record = FailureMarshallers.forJob(failure.job()).marshaller(scope).marshal(failure).set(CrawlerFailureRepository.ID, id);
        records.put(FAILURES, update(using(CrawlerFailureRepository.ID), record));
    }

    @Override
    public Option<Failure> get(UUID id) {
        Record record = records.get(FAILURES).find(where(CrawlerFailureRepository.ID, is(id))).get();
        return option(unmarshal(record));
    }

    @Override
    public void remove(UUID id) {
        records.remove(FAILURES, where(CrawlerFailureRepository.ID, is(id)));
    }

    @Override
    public Sequence<Pair<UUID, Failure>> find(Predicate<? super Record> predicate) {
        return records.get(FAILURES).filter(predicate).map(asPair());
    }

    public int size() {
        return records.get(FAILURES).size();
    }

    public boolean isEmpty() {
        return records.get(FAILURES).isEmpty();
    }

    private Callable1<Record, Pair<UUID, Failure>> asPair() {
        return new Callable1<Record, Pair<UUID, Failure>>() {
            @Override
            public Pair<UUID, Failure> call(Record record) throws Exception {
                return Pair.pair(record.get(ID), unmarshal(record));
            }
        };
    }

    private Failure unmarshal(Record record) {
        return FailureMarshallers.valueOf(record.get(JOB_TYPE)).marshaller(scope).unmarshal(record);
    }

    public int removeAll() {
        return records.remove(FAILURES).intValue();
    }
}
