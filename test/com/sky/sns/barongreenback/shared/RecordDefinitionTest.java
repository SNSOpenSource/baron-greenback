package com.sky.sns.barongreenback.shared;

import com.sky.sns.barongreenback.crawler.Crawler;
import com.sky.sns.barongreenback.crawler.CrawlerTestFixtures;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordDefinitionTest {
    @Test
    public void allFieldsShouldFlatten() throws Exception {
        Sequence<Keyword<?>> keywords = Crawler.methods.keywords(CrawlerTestFixtures.ENTRIES);
        assertThat(keywords.size(), is(7));

    }
}
