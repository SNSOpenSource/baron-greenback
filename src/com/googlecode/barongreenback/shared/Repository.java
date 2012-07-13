package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.Option;

public interface Repository<K,V> {
    Option<V> get(K key);
    void set(K key, V value);
    void remove(K key);
}
