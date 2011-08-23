package com.googlecode.barongreenback.shared;

public interface Repository<K,V> {
    V get(K key);
    void set(K key, V value);
}
