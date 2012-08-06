package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;

import java.util.HashSet;
import java.util.UUID;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings) {
        super(crawlerRepository, checkpointHandler);
        this.mappings = mappings;
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(crawlerId(record), datasource(record), destination(record), lastCheckpointFor(record), moreUri(record), mappings, new HashSet<HttpDatasource>());
        return Failure.failure(job, record.get(FailureRepository.REASON));
    }
}