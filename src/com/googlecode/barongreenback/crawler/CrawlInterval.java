package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

public class CrawlInterval implements Value<Long> {
    public static final String DEFAULT_VALUE = "300";
    public static final String PROPERTY_NAME = "crawler.interval";
    private final long value;

    public CrawlInterval(long value) {
        this.value = value;
    }

    public CrawlInterval(BaronGreenbackProperties properties) {
        this(Long.valueOf(properties.getProperty(PROPERTY_NAME, DEFAULT_VALUE)));
    }

    @Override
    public Long value() {
        return value;
    }
}
