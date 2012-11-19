package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.time.Clock;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings, VisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
        this.mappings = mappings;
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(crawlerId(record), crawledRecord(record), datasource(record), destination(record),
                lastCheckpointFor(record), moreUri(record), mappings, visited.value(), clock.now());
        return Failure.failure(job, record.get(FailureRepository.REASON));
    }
}