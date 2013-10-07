package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import com.googlecode.yadic.Container;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public interface StagedJob {
    HttpDatasource datasource();

    Definition destination();

    Pair<Sequence<Record>, Sequence<StagedJob>> process(Container scope, Response response) throws Exception;

    Set<HttpDatasource> visited();

    UUID crawlerId();

    Record record();

    Date createdDate();

    public static class functions {
        public static Function1<StagedJob, HttpDatasource> datasource() {
            return new Function1<StagedJob, HttpDatasource>() {
                @Override
                public HttpDatasource call(StagedJob job) throws Exception {
                    return job.datasource();
                }
            };
        }

        public static Function1<StagedJob, Date> createdDate() {
            return new Function1<StagedJob, Date>() {
                @Override
                public Date call(StagedJob stagedJob) throws Exception {
                    return stagedJob.createdDate();
                }
            };
        }
    }
}
