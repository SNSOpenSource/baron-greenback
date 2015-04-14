package com.googlecode.barongreenback.crawler.jobs;

import com.googlecode.barongreenback.crawler.datasources.DataSource;
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

public interface Job {
    DataSource dataSource();

    Definition destination();

    Pair<Sequence<Record>, Sequence<Job>> process(Container scope, Response response) throws Exception;

    Set<DataSource> visited();

    UUID crawlerId();

    Record record();

    Date createdDate();

    public static class functions {
        public static Function1<Job, DataSource> dataSource() {
            return new Function1<Job, DataSource>() {
                @Override
                public DataSource call(Job job) throws Exception {
                    return job.dataSource();
                }
            };
        }

        public static Function1<Job, Date> createdDate() {
            return new Function1<Job, Date>() {
                @Override
                public Date call(Job job) throws Exception {
                    return job.createdDate();
                }
            };
        }
    }
}
