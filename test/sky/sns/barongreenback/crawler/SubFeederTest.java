package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import org.junit.Test;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SubFeederTest {
    @Test
    public void shouldOnlyCopyFieldsFromParentIfAbsent() throws Exception {
        Keyword<String> name = keyword("name", String.class);
        Keyword<String> fullname = keyword("fullname", String.class);
        Record parent = record().set(name, "Tom").set(fullname, "Thomas Lee");
        Record subFeed = record().set(name, "Tom Rules");
        Record result = SubFeeder.copyMissingFieldsFromParent(parent, subFeed);

        assertThat(result, is(record().set(name, "Tom Rules").set(fullname, "Thomas Lee")));
    }
}
