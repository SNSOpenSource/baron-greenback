package com.googlecode.barongreenback.less;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.None.none;

public class NoLessCssCache implements LessCssCache {

    @Override
    public Option<CachedLessCss> getOption(String key) {
        return none(CachedLessCss.class);
    }

    @Override
    public void put(String key, CachedLessCss result) {
    }
}
