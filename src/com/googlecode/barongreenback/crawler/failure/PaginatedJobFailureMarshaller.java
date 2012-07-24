package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings) {
        super(crawlerRepository, checkpointHandler);
        this.mappings = mappings;
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(datasource(record), destination(record), lastCheckpointFor(record), moreUri(record), mappings);
        return Failure.failure(job, record.get(CrawlerFailureRepository.REASON));
    }
}