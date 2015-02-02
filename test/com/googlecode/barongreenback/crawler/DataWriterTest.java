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
import java.util.concurrent.TimeUnit;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
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
        Definition fullDefinition = definition(definitionName, id, a, b);
        Definition aPartial = definition(definitionName, id, a);
        Definition bPartial = definition(definitionName, id, b);

        Records records = new MemoryRecords();
        DataWriter dataWriter = new DataWriter(applicationWithRecords(records), 1, 1, "test", 2);
        CountLatch latch = new CountLatch();
        dataWriter.queue(jobWithDefinition(aPartial), one(aRecord), latch);
        dataWriter.queue(jobWithDefinition(bPartial), one(bRecord), latch);
        latch.await(10, TimeUnit.SECONDS);

        assertThat(records.get(fullDefinition).toList(), is(Arrays.asList(Record.methods.merge(aRecord, bRecord))));
    }

    @Test
    public void shouldMergeByDefinitionNameForDefinitionsWithDifferentUniqueField() throws Exception {
        String definitionName = "definition";

        Keyword<String> id1 = keyword("id1", String.class).metadata(Keywords.unique, true);
        Keyword<String> a1 = keyword("a1", String.class);
        Keyword<String> b1 = keyword("b1", String.class);
        Record aRecord1 = record(id1, "1", a1, "isa1");
        Record bRecord1 = record(id1, "1", b1, "isb1");
        Definition definition1 = definition(definitionName, id1, a1, b1);

        Keyword<String> id2 = keyword("id2", String.class).metadata(Keywords.unique, true);
        Keyword<String> a2 = keyword("a2", String.class);
        Keyword<String> b2 = keyword("b2", String.class);
        Record aRecord2 = record(id2, "2", a2, "isa2");
        Record bRecord2 = record(id2, "2", b2, "isb2");
        Record cRecord2 = record(id2, "3", a2, "isa3", b2, "isb3");
        Definition definition2 = definition(definitionName, id2, a2, b2);

        Records records = new MemoryRecords();
        DataWriter dataWriter = new DataWriter(applicationWithRecords(records), 1, 1, "test", 4);
        CountLatch latch = new CountLatch();
        dataWriter.queue(jobWithDefinition(definition1), sequence(aRecord1), latch);
        dataWriter.queue(jobWithDefinition(definition2), sequence(aRecord2), latch);
        latch.await(10, TimeUnit.SECONDS);

        dataWriter.queue(jobWithDefinition(definition1), sequence(bRecord1), latch);
        dataWriter.queue(jobWithDefinition(definition2), sequence(bRecord2, cRecord2), latch);
        latch.await(10, TimeUnit.SECONDS);

        assertThat(records.get(definition1).first(), is(Record.methods.merge(aRecord1, bRecord1)));
        assertThat(records.get(definition2).second(), is(Record.methods.merge(aRecord2, bRecord2)));
        assertThat(records.get(definition2).third(), is(cRecord2));
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
