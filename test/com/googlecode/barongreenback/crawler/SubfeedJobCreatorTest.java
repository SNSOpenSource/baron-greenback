package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.UNIQUE;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SubfeedJobCreatorTest {
    public static final Keyword<String> PERSON_NAME = keyword("person/name", String.class);
    public static final Keyword<Uri> LINK = Keywords.keyword("link", Uri.class).metadata(
            record().
                    set(RecordDefinition.RECORD_DEFINITION, new RecordDefinition(definition("/subfeed", PERSON_NAME))).
                    set(UNIQUE, true));
    public static final Definition SOME_DESTINATION = Definition.constructors.definition("foo", Sequences.<Keyword<?>>empty());
    public static final Uri URI = Uri.uri("http://hello.com/");
    public static final Keyword<String> PREV_UNIQUE = Keywords.keyword("foo", String.class).metadata(record().set(UNIQUE, true));

    @Test
    public void ifRecordContainsSubfeedReturnsJob() throws Exception {
        Sequence<StagedJob> jobs = new SubfeedJobCreator(SubfeedDatasource.datasource(null, null, record()), SOME_DESTINATION).process(one(record().set(LINK, URI))).second();
        assertThat(jobs.size(), NumberMatcher.is(1));
        assertThat(jobs.head().destination(), is(SOME_DESTINATION));
        assertThat(jobs.head().datasource().uri(), is(URI));
    }

    @Test
    public void shouldPassDownKeyAndValuesToSubfeedJobs() throws Exception {
        Record previousUnique = record(one(Pair.<Keyword<?>, Object>pair(PREV_UNIQUE, "bar")));
        Sequence<StagedJob> jobs = new SubfeedJobCreator(SubfeedDatasource.datasource(null, null, previousUnique), SOME_DESTINATION).process(one(record().set(LINK, URI))).second();
        Record record = record(one(Pair.<Keyword<?>, Object>pair(LINK, URI)));
        assertThat(jobs.head().datasource().record(), is(one(record).map(merge(previousUnique)).head()));
    }

    @Test
    public void shouldMergeUniqueKeysIntoEachRecord() throws Exception {
        Pair<Sequence<Record>, Sequence<StagedJob>> records = new SubfeedJobCreator(
                SubfeedDatasource.datasource(null, null, record(one(Pair.<Keyword<?>, Object>pair(LINK, URI)))), SOME_DESTINATION).process(one(record().set(PERSON_NAME, "Dan")));
        assertThat(records.first(), is(one(record().set(PERSON_NAME, "Dan").set(LINK, URI))));
    }
}
