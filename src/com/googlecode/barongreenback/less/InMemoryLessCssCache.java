package com.googlecode.barongreenback.less;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLessCssCache implements LessCssCache {
    
    static private Map<String, CachedLessCss> cache = new ConcurrentHashMap<String, CachedLessCss>();
    
    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    @Override
    public CachedLessCss get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, CachedLessCss result) {
        cache.put(key, result);
    }
}
