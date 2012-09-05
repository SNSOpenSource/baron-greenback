package com.googlecode.barongreenback.less;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLessCssCache implements LessCssCache {
    
    static private Map<String, String> cache = new ConcurrentHashMap<String, String>();
    
    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    @Override
    public String get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, String result) {
        cache.put(key, result);
    }
}
