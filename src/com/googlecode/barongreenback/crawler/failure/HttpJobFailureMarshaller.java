package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class HttpJobFailureMarshaller extends AbstractFailureMarshaller {
    public HttpJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler) {
        super(crawlerRepository, checkpointHandler);
    }

    @Override
    public Failure unmarshal(Record record) {
        HttpJob job = HttpJob.httpJob(datasource(record), destination(record));
        return Failure.failure(job, record.get(CrawlerFailureRepository.REASON));
    }
}
