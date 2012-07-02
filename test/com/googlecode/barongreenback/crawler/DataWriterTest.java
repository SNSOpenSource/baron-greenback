package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.StringPrintStream;
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DataWriterTest {
    @Test
    public void shouldWriteUniqueDataToRecords() throws Exception {
        Records records = new MemoryRecords();
        Keyword<String> name = Keywords.keyword("name", String.class).metadata(record().set(Keywords.UNIQUE, true));
        Definition children = definition("children", name);
        Record expected = record().set(name, "Dan");
        DataWriter dataWriter = new DataWriter(records, new StringPrintStream());
        dataWriter.writeUnique(children, one(expected));
        dataWriter.writeUnique(children, one(expected));
        Sequence<Record> result = records.get(children);
        assertThat(result, is(one(expected)));
    }

    @Test
    @Ignore("Need to discuss aliased keywords")
    public void shouldBeExactlyOneUniqueField() {
        Records records = new MemoryRecords();
        Keyword<String> field1 = Keywords.keyword("field1", String.class).metadata(record().set(Keywords.UNIQUE, true));
        Keyword<String> field2 = Keywords.keyword("field2", String.class).metadata(record().set(Keywords.UNIQUE, true));
        Definition children = definition("children", field1, field2);
        Record record = record().set(field1, "some value").set(field2, "some other value");
        try {
            new DataWriter(records, new StringPrintStream()).writeUnique(children, one(record));
            fail("An " + IllegalStateException.class.getSimpleName() + " exception should have been thrown");
        } catch (Exception e) {
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
        }
    }
}