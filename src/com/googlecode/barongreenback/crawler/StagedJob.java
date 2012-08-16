package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Set;
import java.util.UUID;

public interface StagedJob {
    HttpDatasource datasource();

    Definition destination();

    Function1<Response, Pair<Sequence<Record>, Sequence<StagedJob>>> process(Container crawlerScope);

    Set<HttpDatasource> visited();

    UUID crawlerId();

    Record record();

    public static class functions {
        public static Callable1<StagedJob, HttpDatasource> datasource() {
            return new Callable1<StagedJob, HttpDatasource>() {
                @Override
                public HttpDatasource call(StagedJob job) throws Exception {
                    return job.datasource();
                }
            };
        }
    }
}
