package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.datasources.DataSource;
import com.googlecode.barongreenback.crawler.datasources.HttpDataSource;
import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import java.util.Set;

import static com.googlecode.totallylazy.Sets.fifoSet;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

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
        return fifoSet(size);
    }
}