package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpVisitedFactory;
import com.googlecode.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.time.Clock;

import static com.googlecode.barongreenback.crawler.failures.FailureRepository.DURATION;

public class MasterPaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public MasterPaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, BaronGreenbackStringMappings mappings, HttpVisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
        this.mappings = mappings.value();
    }

    @Override
    public Failure unmarshal(Record record) {
        MasterPaginatedHttpJob job = MasterPaginatedHttpJob.masterPaginatedHttpJob(crawlerId(record), dataSource(record), destination(record), lastCheckpointFor(record), moreUri(record), visited, clock);
        return Failure.failure(job, record.get(FailureRepository.REASON), record.get(DURATION));
    }
}