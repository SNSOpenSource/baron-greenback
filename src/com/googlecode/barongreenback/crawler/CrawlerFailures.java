package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Pair;
import com.googlecode.utterlyidle.Response;

import java.util.*;

public class CrawlerFailures implements StatusMonitor {
    private final Map<UUID, Pair<StagedJob<Response>, Response>> failures;

    public CrawlerFailures() {
        this.failures = new HashMap<UUID, Pair<StagedJob<Response>, Response>>();
    }

    @Override
    public String name() {
        return "Retry Queue";
    }

    @Override
    public int activeThreads() {
        return 0;
    }

    @Override
    public int size() {
        return failures.size();
    }

    public void add(Pair<StagedJob<Response>, Response> failure) {
        failures.put(UUID.randomUUID(), failure);
    }

    public Map<UUID, Pair<StagedJob<Response>, Response>> values() {
        return failures;
    }

    public void delete(UUID id) {
        failures.remove(id);
    }

    public Pair<StagedJob<Response>, Response> get(UUID id) {
        return failures.get(id);
    }
}