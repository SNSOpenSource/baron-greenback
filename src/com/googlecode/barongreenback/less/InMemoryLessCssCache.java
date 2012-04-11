package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Uri;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLessCssCache implements LessCssCache {
    
    private Map<Uri, String> cache = new ConcurrentHashMap<Uri, String>();
    
    @Override
    public boolean containsKey(Uri uri) {
        return cache.containsKey(uri);
    }

    @Override
    public String get(Uri uri) {
        return cache.get(uri);
    }

    @Override
    public void put(Uri uri, String result) {
        cache.put(uri, result);
    }
}
