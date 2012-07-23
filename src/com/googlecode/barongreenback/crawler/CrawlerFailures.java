package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CrawlerFailures implements StatusMonitor {
    public static final int MAX_FAILURES = 100000;
    private final Map<UUID, Failure> failures = new LinkedHashMap<UUID, Failure>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Failure> eldest) {
            return size() > MAX_FAILURES;
        }
    };

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
        return failures.size();
    }

    public void add(Failure failure) {
        failures.put(UUID.randomUUID(), failure);
    }

    public Map<UUID, Failure> values() {
        return failures;
    }

    public void delete(UUID id) {
        failures.remove(id);
    }

    public Option<Failure> get(UUID id) {
        return Option.option(failures.get(id));
    }

    public boolean isEmpty() {
        return failures.isEmpty();
    }

}