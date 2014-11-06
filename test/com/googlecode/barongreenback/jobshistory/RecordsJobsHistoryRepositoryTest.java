package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.persistence.InMemoryPersistentTypesActivator;
import com.googlecode.barongreenback.search.ParserUkDateConverter;
import com.googlecode.barongreenback.search.PredicateBuilder;
import com.googlecode.barongreenback.search.StandardParserActivator;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import com.googlecode.lazyrecords.parser.PredicateParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.barongreenback.persistence.BaronGreenbackRecords.records;
import static com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings.baronGreenbackStringMappings;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordsJobsHistoryRepositoryTest {
    RecordsJobsHistoryRepository repository;

    @Before
    public void initialiseRepository() throws Exception {
        final BaronGreenbackStringMappings baronGreenbackStringMappings = baronGreenbackStringMappings(new StringMappings(), new InMemoryPersistentTypesActivator().call());
        final PredicateParser parser = new StandardParserActivator(new ParserUkDateConverter(), baronGreenbackStringMappings).call();
        repository = new RecordsJobsHistoryRepository(records(new MemoryRecords(baronGreenbackStringMappings.value())), new PredicateBuilder(parser, baronGreenbackStringMappings));
    }

    @Test
    public void deletesNothing() throws Exception {
        repository.put(aJobHistoryItemFrom(new Date()));
        repository.remove("nonMatchingQuery");
        assertThat(repository.find("").right().size(), is(1));
    }

    @Test
    public void deletesEverything() throws Exception {
        repository.put(aJobHistoryItemFrom(new Date()));
        repository.remove("");
        assertThat(repository.find("").right().size(), is(0));
    }

    @Test
    public void deletesSome() throws Exception {
        final JobId jobId = new JobId(UUID.randomUUID());
        repository.put(aJobHistoryItemWith(jobId));
        repository.put(aJobHistoryItem());
        repository.remove(format("jobId:\"%s\"", jobId.value().toString()));
        assertThat(repository.find("").right().size(), is(1));
    }

    private JobHistoryItem aJobHistoryItem() {
        return new JobHistoryItem(new JobId(UUID.randomUUID()), 0, new Date(), "", none(String.class));
    }

    private JobHistoryItem aJobHistoryItemWith(JobId jobId) {
        return new JobHistoryItem(jobId, 0, new Date(), "", none(String.class));
    }

    private JobHistoryItem aJobHistoryItemFrom(Date timestamp) {
        return new JobHistoryItem(new JobId(UUID.randomUUID()), 0, timestamp, "", none(String.class));
    }
}