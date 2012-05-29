package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Subfeeder2Test {
    private static final Keyword<String> PERSON_NAME = keyword("person/name", String.class);
    public static final Keyword<Uri> LINK = Keywords.keyword("link", Uri.class).metadata(
            record().set(RecordDefinition.RECORD_DEFINITION, new RecordDefinition(definition("/subfeed", PERSON_NAME))));

    @Test
    public void ifRecordContainsSubfeedReturnsJob() throws Exception {
        Definition destination = Definition.constructors.definition(null, null);
        Uri expectedUri = Uri.uri("http://domain/somewhere");
        Sequence<Job> jobs = Subfeeder2.subfeeds(one(record().set(LINK, expectedUri)), destination);
        assertThat(jobs.size(), NumberMatcher.is(1));
        assertThat(jobs.head().destination(), is(destination));
        assertThat(jobs.head().dataSource().uri(), is(expectedUri));
    }
}
