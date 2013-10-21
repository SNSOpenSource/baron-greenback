package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.datasources.DataSource;
import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Collections.newSetFromMap;

public class HttpVisitedFactory implements Value<Set<DataSource>> {
    public static final String PROPERTY_NAME = "crawler.visited.size";
    public static final int DEFAULT_SIZE = 1000;
    private final int size;

    private HttpVisitedFactory(int size) {
        this.size = size;
    }

    public static HttpVisitedFactory visitedFactory() { return visitedFactory(DEFAULT_SIZE); }

    public static HttpVisitedFactory visitedFactory(int size) {return new HttpVisitedFactory(size);}

    public static HttpVisitedFactory visitedFactory(BaronGreenbackProperties properties) {
        return visitedFactory(parseInt(properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT_SIZE))));
    }

    public Set<DataSource> value() {
        return concurrentFifoSet(size);
    }

    private Set<DataSource> concurrentFifoSet(int size) {
        return newSetFromMap(HttpVisitedFactory.<DataSource, Boolean>concurrentFifoMap(size));
    }

    private static <K, V> Map<K, V> concurrentFifoMap(final int maximumElements) {
        return new ConcurrentHashMap<K, V>(maximumElements) {
            Queue<K> entryOrder = new ConcurrentLinkedQueue<K>();

            @Override
            public V put(K key, V value) {
                V result = super.put(key, value);
                entryOrder.offer(key);

                if (size() > maximumElements) {
                    remove(eldestEntry());
                }

                return result;
            }

            private K eldestEntry() {
                return entryOrder.poll();
            }
        };
    }
}