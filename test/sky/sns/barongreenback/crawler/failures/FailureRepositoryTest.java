package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.memory.MemoryRecords;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class FailureRepositoryTest {

    public static final UUID FAILURE_ID = UUID.randomUUID();
    public static final UUID CRAWLER_ID = UUID.randomUUID();
    private static final UUID ANOTHER_CRAWLER_ID = UUID.randomUUID();
    private static final java.util.UUID ANOTHER_FAILURE_ID = UUID.randomUUID();

    private FailureRepository failureRepository;

    @Test
    public void shouldDeleteFailuresForACrawler() throws Exception {
        final Record record = Record.constructors.record(FailureRepository.CRAWLER_ID, CRAWLER_ID);
        final Record record2 = Record.constructors.record(FailureRepository.CRAWLER_ID, ANOTHER_CRAWLER_ID);

        failureRepository = new FailureRepository(BaronGreenbackRecords.records(new MemoryRecords()));
        failureRepository.set(FAILURE_ID, record);
        failureRepository.set(ANOTHER_FAILURE_ID, record2);

        failureRepository.removeAllForCrawler(CRAWLER_ID);

        assertTrue(failureRepository.get(FAILURE_ID).isEmpty());
        assertTrue(failureRepository.get(ANOTHER_FAILURE_ID).isDefined());
    }


}