package com.googlecode.barongreenback.crawler;

import java.util.UUID;

public interface Crawler {
    Number crawl(UUID id) throws Exception;
}
