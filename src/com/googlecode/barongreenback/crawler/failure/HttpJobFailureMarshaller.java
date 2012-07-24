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
    public Record marshal(Failure failure) {
        return record().
                set(CrawlerFailureRepository.type, nameForClass(failure.job().getClass())).
                set(CrawlerFailureRepository.reason, failure.reason()).
                set(CrawlerFailureRepository.source, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                set(CrawlerFailureRepository.record, toJson(failure.job().datasource().record())).
                set(CrawlerFailureRepository.crawlerId, failure.job().datasource().crawlerId()).
                set(CrawlerFailureRepository.uri, failure.job().datasource().uri());
    }

    @Override
    public Failure unmarshal(Record record) {
        HttpJob job = HttpJob.httpJob(datasource(record), destination(record));
        return Failure.failure(job, record.get(CrawlerFailureRepository.reason));
    }
}
