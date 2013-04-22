package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

public interface LessCssCache {
    Option<CachedLessCss> get(String key);

    void put(String key, CachedLessCss result);
}
