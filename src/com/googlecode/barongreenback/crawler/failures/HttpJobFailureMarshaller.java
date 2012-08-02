package com.googlecode.barongreenback.crawler.failures;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.HttpDatasource;
import com.googlecode.barongreenback.crawler.HttpJob;
import com.googlecode.lazyrecords.Record;

import java.util.HashSet;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class HttpJobFailureMarshaller extends AbstractFailureMarshaller {
    public HttpJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler) {
        super(crawlerRepository, checkpointHandler);
    }

    @Override
    public Failure unmarshal(Record record) {
        HttpJob job = HttpJob.httpJob(datasource(record), destination(record), new HashSet<HttpDatasource>());
        return Failure.failure(job, record.get(FailureRepository.REASON));
    }
}
