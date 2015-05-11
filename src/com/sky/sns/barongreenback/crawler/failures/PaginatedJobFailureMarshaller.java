package com.sky.sns.barongreenback.crawler.failures;

import com.sky.sns.barongreenback.crawler.CheckpointHandler;
import com.sky.sns.barongreenback.crawler.CrawlerRepository;
import com.sky.sns.barongreenback.crawler.HttpVisitedFactory;
import com.sky.sns.barongreenback.crawler.jobs.PaginatedHttpJob;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;

import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.DURATION;
import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.REASON;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, HttpVisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(crawlerId(record), crawledRecord(record), dataSource(record), destination(record),
                lastCheckpointFor(record), moreUri(record), visited.value(), clock.now());
        return Failure.failure(job, record.get(REASON), record.get(DURATION));
    }
}