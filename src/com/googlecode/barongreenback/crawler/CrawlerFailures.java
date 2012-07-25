package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failure.CrawlerFailureRepository;
import com.googlecode.barongreenback.crawler.failure.FailureMarshallers;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Container;

import java.util.UUID;

import static com.googlecode.barongreenback.crawler.failure.CrawlerFailureRepository.ID;
import static com.googlecode.totallylazy.Predicates.all;

public class CrawlerFailures implements StatusMonitor {
    private final CrawlerFailureRepository repository;
    private final Container scope;

    public CrawlerFailures(CrawlerFailureRepository repository, Container scope) {
        this.repository = repository;
        this.scope = scope;
    }

    @Override
    public String name() {
        return "Retry Queue";
    }

    @Override
    public Option<Integer> activeThreads() {
        return Option.none();
    }

    @Override
    public int size() {
        return repository.size();
    }

    public UUID add(Failure failure) {
        UUID id = UUID.randomUUID();
        repository.set(id, FailureMarshallers.forJob(failure.job()).marshaller(scope).marshal(failure));
        return id;
    }

    public Sequence<Pair<UUID, Failure>> values() {
        return repository.find(all()).map(asPair());
    }

    public void delete(UUID id) {
        repository.remove(id);
    }

    public Option<Failure> get(UUID id) {
        return repository.get(id).map(unmarshal());
    }

    public boolean isEmpty() {
        return repository.isEmpty();
    }

    public int removeAll() {
        return repository.removeAll();
    }

    private Failure unmarshal(Record record) {
        return FailureMarshallers.valueOf(record.get(CrawlerFailureRepository.JOB_TYPE)).marshaller(scope).unmarshal(record);
    }

    private Callable1<Record, Failure> unmarshal() {
        return new Callable1<Record, Failure>() {
            @Override
            public Failure call(Record record) throws Exception {
                return CrawlerFailures.this.unmarshal(record);
            }
        };
    }

    private Callable1<Record, Pair<UUID, Failure>> asPair() {
        return new Callable1<Record, Pair<UUID, Failure>>() {
            @Override
            public Pair<UUID, Failure> call(Record record) throws Exception {
                return Pair.pair(record.get(ID), unmarshal(record));
            }
        };
    }
}