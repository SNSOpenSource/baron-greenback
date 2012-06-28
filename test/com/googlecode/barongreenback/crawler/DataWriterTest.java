package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Ignore;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataWriterTest {
    @Test
    public void shouldNotWriteDataToRecordsWhenNotUnique() throws Exception {
        Records records = new MemoryRecords();
        Keyword<String> name = Keywords.keyword("name", String.class);
        Definition children = definition("children", name);
        Record expected = record().set(name, "Dan");
        new DataWriter(records).writeUnique(children, one(expected));
        Sequence<Record> result = records.get(children);
        assertThat(result, is(Sequences.<Record>empty()));
    }

    @Test
    public void shouldWriteUniqueDataToRecords() throws Exception {
        Records records = new MemoryRecords();
        Keyword<String> name = Keywords.keyword("name", String.class).metadata(record().set(Keywords.UNIQUE, true));
        Definition children = definition("children", name);
        Record expected = record().set(name, "Dan");
        DataWriter dataWriter = new DataWriter(records);
        dataWriter.writeUnique(children, one(expected));
        dataWriter.writeUnique(children, one(expected));
        Sequence<Record> result = records.get(children);
        assertThat(result, is(one(expected)));
    }
}