package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.yadic.Container;

public interface StagedJob<I> {
    Container container();

    HttpDatasource dataSource();

    Definition destination();

    Function1<I, Pair<Sequence<Record>, Sequence<StagedJob<I>>>> process();
}
