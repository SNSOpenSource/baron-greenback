package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpVisitedFactory;
import com.googlecode.barongreenback.crawler.jobs.HttpJob;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;

import static com.googlecode.barongreenback.crawler.failures.FailureRepository.DURATION;

public class HttpJobFailureMarshaller extends AbstractFailureMarshaller {
    public HttpJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, HttpVisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
    }

    @Override
    public Failure unmarshal(Record record) {
        HttpJob job = HttpJob.httpJob(crawlerId(record), crawledRecord(record), dataSource(record), destination(record), visited.value(), clock.now());
        return Failure.failure(job, record.get(FailureRepository.REASON), record.get(DURATION));
    }
}
