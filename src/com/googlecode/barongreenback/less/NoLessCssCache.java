package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.None.none;

public enum NoLessCssCache implements LessCssCache {
    instance;

    @Override
    public Option<CachedLessCss> get(String key) {
        return none(CachedLessCss.class);
    }

    @Override
    public boolean put(String key, CachedLessCss result) {
        return false;
    }
}
