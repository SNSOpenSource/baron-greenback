package sky.sns.barongreenback.crawler;

import sky.sns.barongreenback.crawler.datasources.DataSource;
import sky.sns.barongreenback.crawler.datasources.HttpDataSource;
import sky.sns.barongreenback.crawler.jobs.HttpSubfeedJobCreator;
import sky.sns.barongreenback.crawler.jobs.Job;
import sky.sns.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static sky.sns.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Keywords.unique;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpSubfeedJobCreatorTest {
    public static final Keyword<String> PERSON_NAME = keyword("person/name", String.class);
    public static final Keyword<Uri> LINK = keyword("link", Uri.class).
            metadata(RECORD_DEFINITION, new RecordDefinition(definition("/subfeed", PERSON_NAME))).
            metadata(unique, true);
    public static final Definition SOME_DESTINATION = Definition.constructors.definition("foo", Sequences.<Keyword<?>>empty());
    public static final Uri URI = Uri.uri("http://hello.com/");
    public static final Keyword<String> PREV_UNIQUE = keyword("foo", String.class).metadata(unique, true);
    private final Date createdDate = date(2001, 1, 1);

    @Test
    public void ifRecordContainsSubfeedReturnsJob() throws Exception {
        UUID crawlerId = UUID.randomUUID();
        Sequence<Job> jobs = new HttpSubfeedJobCreator(SOME_DESTINATION, new HashSet<DataSource>(), crawlerId, record(), createdDate).process(one(record().set(LINK, URI))).second();
        assertThat(jobs.size(), NumberMatcher.is(1));
        assertThat(jobs.head().destination(), is(SOME_DESTINATION));
        assertThat(jobs.head().dataSource().uri(), is(URI));
    }

    @Test
    public void shouldPassDownKeyAndValuesToSubfeedJobs() throws Exception {
        Record previousUnique = record(one(Pair.<Keyword<?>, Object>pair(PREV_UNIQUE, "bar")));
        UUID crawlerId = UUID.randomUUID();
        Sequence<Job> jobs = new HttpSubfeedJobCreator(SOME_DESTINATION, new HashSet<DataSource>(), crawlerId, previousUnique, createdDate).process(one(record().set(LINK, URI))).second();
        Record record = record(one(Pair.<Keyword<?>, Object>pair(LINK, URI)));
        assertThat(jobs.head().record(), is(one(record).map(merge(previousUnique)).head()));
    }

    @Test
    public void shouldMergeUniqueKeysIntoEachRecord() throws Exception {
        Record previousRecord = record(one(Pair.<Keyword<?>, Object>pair(LINK, URI)));
        UUID crawlerId = UUID.randomUUID();
        Pair<Sequence<Record>, Sequence<Job>> records = new HttpSubfeedJobCreator(
                SOME_DESTINATION, new HashSet<DataSource>(), crawlerId, previousRecord, createdDate).process(one(record().set(PERSON_NAME, "Dan")));
        assertThat(records.first(), is(one(record().set(PERSON_NAME, "Dan").set(LINK, URI))));
    }
}
