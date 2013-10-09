package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.crawler.datasources.DataSource;
import com.googlecode.barongreenback.crawler.datasources.HttpDataSource;
import com.googlecode.barongreenback.crawler.jobs.Job;
import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.CountLatch;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Application;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.modules.ApplicationScopedModule;
import com.googlecode.yadic.Container;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.utterlyidle.ApplicationBuilder.application;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataWriterTest {

    @Test
    public void shouldMergeByDefinitionName() throws Exception {
        Keyword<String> id = keyword("id", String.class).metadata(Keywords.unique, true);
        Keyword<String> a = keyword("a", String.class);
        Keyword<String> b = keyword("b", String.class);

        Record aRecord = record(id, "1", a, "isa");
        Record bRecord = record(id, "1", b, "isb");

        String definitionName = "definition";
        Definition fullDefinition = Definition.constructors.definition(definitionName, id, a, b);
        Definition aPartial = Definition.constructors.definition(definitionName, id, a);
        Definition bPartial = Definition.constructors.definition(definitionName, id, b);

        Records records = new MemoryRecords();
        DataWriter dataWriter = new DataWriter(applicationWithRecords(records), 1, 1, "test", 2);
        CountLatch latch = new CountLatch();
        dataWriter.queue(jobWithDefinition(aPartial), one(aRecord), latch);
        dataWriter.queue(jobWithDefinition(bPartial), one(bRecord), latch);
        latch.await();

        assertThat(records.get(fullDefinition).toList(), is(Arrays.asList(Record.methods.merge(aRecord, bRecord))));
    }

    private Application applicationWithRecords(final Records records) {
        return application().add(new ApplicationScopedModule() {
            @Override
            public Container addPerApplicationObjects(Container container) throws Exception {
                return container.addInstance(BaronGreenbackRecords.class, BaronGreenbackRecords.records(records));
            }
        }).build();
    }

    private Job jobWithDefinition(final Definition definition) {
        return new Job() {
            @Override
            public DataSource dataSource() {
                throw new RuntimeException("not done yet");
            }

            @Override
            public Definition destination() {
                return definition;
            }

            @Override
            public Pair<Sequence<Record>, Sequence<Job>> process(Container scope, Response response) throws Exception {
                throw new RuntimeException("not done yet");
            }

            @Override
            public Set<DataSource> visited() {
                throw new RuntimeException("not done yet");
            }

            @Override
            public UUID crawlerId() {
                throw new RuntimeException("not done yet");
            }

            @Override
            public Record record() {
                throw new RuntimeException("not done yet");
            }

            @Override
            public Date createdDate() {
                throw new RuntimeException("not done yet");
            }
        };
    }
}
