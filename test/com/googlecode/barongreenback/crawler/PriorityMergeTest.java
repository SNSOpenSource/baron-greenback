package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import org.junit.Test;

import static com.googlecode.barongreenback.crawler.PriorityMerge.priorityMerge;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PriorityMergeTest {
    @Test
    public void canMergeTwoRecordsBasedOnTheirKeywordPriority() throws Exception {
        Keyword<String> baseKey = keyword("name", String.class);
        Keyword<String> lowKey = baseKey.metadata(record().set(RecordDefinition.PRIORITY, "Z"));
        Record low = record().set(lowKey, "Dan");
        Keyword<String> hiKey = baseKey.metadata(record().set(RecordDefinition.PRIORITY, "A"));
        Record hi = record().set(hiKey, "Tom");

        assertThat(priorityMerge(low).apply(hi).get(baseKey), is("Tom"));
        assertThat(priorityMerge(hi).apply(low).get(baseKey), is("Tom"));
    }

    @Test
    public void keywordsWithPriorityOverride() throws Exception {
        Keyword<String> baseKey = keyword("name", String.class);
        Record low = record().set(baseKey, "Dan");
        Keyword<String> hiKey = baseKey.metadata(record().set(RecordDefinition.PRIORITY, "A"));
        Record hi = record().set(hiKey, "Tom");

        assertThat(priorityMerge(low).apply(hi).get(baseKey), is("Tom"));
        assertThat(priorityMerge(hi).apply(low).get(baseKey), is("Tom"));
    }

    @Test
    public void ifNoPriorityParentWins() throws Exception {
        Keyword<String> baseKey = keyword("name", String.class);
        Record low = record().set(baseKey, "Dan");
        Record hi = record().set(baseKey, "Tom");

        assertThat(priorityMerge(low).apply(hi).get(baseKey), is("Dan"));
        assertThat(priorityMerge(hi).apply(low).get(baseKey), is("Tom"));
    }

}
