package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCompiledLessCache implements CompiledLessCache {
    private static final Map<String, CompiledLess> cache = new ConcurrentHashMap<String, CompiledLess>();

    @Override
    public Option<CompiledLess> get(String key) {
        return Option.option(cache.get(key));
    }

    @Override
    public boolean put(String key, CompiledLess result) {
        cache.put(key, result);
        return true;
    }
}