package com.sky.sns.barongreenback.crawler.failures;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Clock;
import com.sky.sns.barongreenback.crawler.CheckpointHandler;
import com.sky.sns.barongreenback.crawler.CrawlerRepository;
import com.sky.sns.barongreenback.crawler.HttpVisitedFactory;
import com.sky.sns.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import com.sky.sns.barongreenback.persistence.BaronGreenbackStringMappings;

import static com.sky.sns.barongreenback.crawler.failures.FailureRepository.DURATION;

public class MasterPaginatedJobFailureMarshaller extends AbstractFailureMarshaller {

    public MasterPaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, BaronGreenbackStringMappings mappings, HttpVisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, mappings, clock);
    }

    @Override
    public Failure unmarshal(Record record) {
        MasterPaginatedHttpJob job = MasterPaginatedHttpJob.masterPaginatedHttpJob(crawlerId(record), dataSource(record), destination(record), lastCheckpointFor(record), moreUri(record), visited, clock);
        return Failure.failure(job, record.get(FailureRepository.REASON), record.get(DURATION));
    }
}