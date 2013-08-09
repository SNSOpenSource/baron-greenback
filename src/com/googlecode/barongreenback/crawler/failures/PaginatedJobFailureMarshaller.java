package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;

import static com.googlecode.barongreenback.crawler.failures.FailureRepository.DURATION;
import static com.googlecode.barongreenback.crawler.failures.FailureRepository.REASON;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, VisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(crawlerId(record), crawledRecord(record), datasource(record), destination(record),
                lastCheckpointFor(record), moreUri(record), visited.value(), clock.now());
        return Failure.failure(job, record.get(REASON), record.get(DURATION));
    }
}