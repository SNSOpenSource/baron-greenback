package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLessCssCache implements LessCssCache {
    private static final Map<String, CachedLessCss> cache = new ConcurrentHashMap<String, CachedLessCss>();

    @Override
    public Option<CachedLessCss> getOption(String key) {
        return Option.option(cache.get(key));
    }

    @Override
    public void put(String key, CachedLessCss result) {
        cache.put(key, result);
    }
}