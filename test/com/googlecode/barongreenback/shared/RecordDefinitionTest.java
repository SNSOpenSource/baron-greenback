package com.googlecode.barongreenback.shared;

import com.googlecode.barongreenback.crawler.Crawler;
import com.googlecode.barongreenback.crawler.CrawlerTestFixtures;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class RecordDefinitionTest {
    @Test
    public void allFieldsShouldFlatten() throws Exception {
        Sequence<Keyword<?>> keywords = Crawler.methods.keywords(CrawlerTestFixtures.ENTRIES);
        assertThat(keywords.size(), NumberMatcher.is(6));

    }
}
