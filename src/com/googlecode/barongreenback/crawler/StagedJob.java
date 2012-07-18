package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

public interface StagedJob {
    Container container();

    HttpDatasource datasource();

    Definition destination();

    Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process();
}
