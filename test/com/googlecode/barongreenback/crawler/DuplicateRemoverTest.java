package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.CrawlerTestFixtures.ATOM_DEFINITION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DuplicateRemoverTest {
    @Test
    public void handleNoRecords() throws Exception {
        Sequence<Record> records = DuplicateRemover.filterDuplicates(ATOM_DEFINITION.definition(), Sequences.<Record>empty());
        assertThat(records.isEmpty(), is(true));
    }
}
