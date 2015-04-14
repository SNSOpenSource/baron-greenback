package com.googlecode.barongreenback.crawler;

import com.googlecode.totallylazy.Option;

public interface StatusMonitor {
    String name();

    Option<Integer> activeThreads();

    int size();
}
