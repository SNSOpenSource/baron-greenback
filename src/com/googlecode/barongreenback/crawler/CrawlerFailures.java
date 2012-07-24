package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.failure.CrawlerFailureRepository;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.util.Map;
import java.util.UUID;

import static com.googlecode.totallylazy.Predicates.all;

public class CrawlerFailures implements StatusMonitor {
    private final CrawlerFailureRepository repository;

    public CrawlerFailures(CrawlerFailureRepository repository) {
        this.repository = repository;
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

    public void add(Failure failure) {
        repository.set(UUID.randomUUID(), failure);
    }

    public Sequence<Pair<UUID, Failure>> values() {
        return repository.find(all());
    }

    public void delete(UUID id) {
        repository.remove(id);
    }

    public Option<Failure> get(UUID id) {
        return repository.get(id);
    }

    public boolean isEmpty() {
        return repository.isEmpty();
    }
}