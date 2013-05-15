package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.barongreenback.crawler.VisitedFactory;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;

import static com.googlecode.barongreenback.crawler.failures.FailureRepository.DURATION;

public class HttpJobFailureMarshaller extends AbstractFailureMarshaller {
    public HttpJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, VisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
    }

    @Override
    public Failure unmarshal(Record record) {
        HttpJob job = HttpJob.httpJob(crawlerId(record), crawledRecord(record), datasource(record), destination(record), visited.value(), clock.now());
        return Failure.failure(job, record.get(FailureRepository.REASON), record.get(DURATION));
    }
}
