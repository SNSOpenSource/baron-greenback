package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.BaronGreenbackProperties;
import com.googlecode.totallylazy.Value;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class CrawlerConnectTimeout implements Value<Integer> {
    public static final String PROPERTY_NAME = "crawler.connect.timeout";
    public static final int DEFAULT = 50;
    private final int value;

    public CrawlerConnectTimeout(BaronGreenbackProperties properties) {
        this.value = parseInt(properties.getProperty(PROPERTY_NAME, valueOf(DEFAULT)));
    }

    @Override
    public Integer value() {
        return value;
    }
}
