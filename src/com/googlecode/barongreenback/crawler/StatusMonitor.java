package com.googlecode.barongreenback.crawler;

public interface StatusMonitor {
    String name();

    int activeThreads();

    int size();
}
