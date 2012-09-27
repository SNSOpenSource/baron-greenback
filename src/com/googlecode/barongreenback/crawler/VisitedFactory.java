package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Value;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Collections.newSetFromMap;

public class VisitedFactory implements Value<Set<HttpDatasource>> {
    private static final String NAME = "crawler.visited.size";
    public static final int DEFAULT_SIZE = 1000;
    private final int size;

    private VisitedFactory(int size) {
        this.size = size;
    }

    public static VisitedFactory visitedFactory() { return visitedFactory(DEFAULT_SIZE); }

    public static VisitedFactory visitedFactory(int size) {return new VisitedFactory(size);}

    public static VisitedFactory visitedFactory(Properties properties) {
        return visitedFactory(parseInt(properties.getProperty(NAME, valueOf(DEFAULT_SIZE))));
    }

    public Set<HttpDatasource> value() {
        return fixedSizeSet(size);
    }

    public static <T> Set<T> fixedSizeSet(final int capacity) {
        return newSetFromMap(new LinkedHashMap<T, Boolean>(capacity) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, Boolean> eldest) {
                return size() > capacity;
            }
        });
    }
}
