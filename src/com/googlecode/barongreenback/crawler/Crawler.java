package com.googlecode.barongreenback.crawler;

import java.io.PrintStream;
import java.util.UUID;

public interface Crawler {
    Number crawl(UUID id, PrintStream log) throws Exception;
}
