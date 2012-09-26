package com.googlecode.barongreenback.less;

public interface LessCssCache {

    boolean containsKey(String key);

    String get(String key);

    void put(String key, String result);
}
