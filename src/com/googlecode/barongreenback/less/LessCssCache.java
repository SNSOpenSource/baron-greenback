package com.googlecode.barongreenback.less;

public interface LessCssCache {

    boolean containsKey(String key);

    CachedLessCss get(String key);

    void put(String key, CachedLessCss result);
}
