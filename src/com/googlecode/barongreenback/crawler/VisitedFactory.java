package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import java.util.Set;

import static com.googlecode.totallylazy.Sets.fifoSet;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class VisitedFactory implements Value<Set<HttpDatasource>> {
    public static final String PROPERTY_NAME = "crawler.visited.size";
    public static final int DEFAULT_SIZE = 1000;
    private final int size;

    private VisitedFactory(int size) {
        this.size = size;
    }

    public static VisitedFactory visitedFactory() { return visitedFactory(DEFAULT_SIZE); }

    public static VisitedFactory visitedFactory(int size) {return new VisitedFactory(size);}

    public static VisitedFactory visitedFactory(BaronGreenbackProperties properties) {
        return visitedFactory(parseInt(properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT_SIZE))));
    }

    public Set<HttpDatasource> value() {
        return fifoSet(size);
    }
}