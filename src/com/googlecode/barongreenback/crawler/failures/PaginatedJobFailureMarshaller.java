package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings, VisitedFactory visitedFactory) {
        super(crawlerRepository, checkpointHandler, visitedFactory);
        this.mappings = mappings;
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(crawlerId(record), crawledRecord(record), datasource(record), destination(record), lastCheckpointFor(record), moreUri(record), mappings,
                visited.value());
        return Failure.failure(job, record.get(FailureRepository.REASON));
    }
}