package com.googlecode.barongreenback.crawler.datasources;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Uri;

public interface DataSource {
    Uri uri();

    Definition source();
}
