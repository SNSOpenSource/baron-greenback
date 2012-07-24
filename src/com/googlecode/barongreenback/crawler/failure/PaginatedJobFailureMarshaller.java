package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class PaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public PaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings) {
        super(crawlerRepository, checkpointHandler);
        this.mappings = mappings;
    }

    @Override
    public Record marshal(Failure failure) {
        return record().
                set(CrawlerFailureRepository.TYPE, nameForClass(failure.job().getClass())).
                set(CrawlerFailureRepository.REASON, failure.reason()).
                set(CrawlerFailureRepository.SOURCE, RecordDefinition.toModel(failure.job().datasource().source()).toString()).
                set(CrawlerFailureRepository.RECORD, toJson(failure.job().datasource().record())).
                set(CrawlerFailureRepository.CRAWLER_ID, failure.job().datasource().crawlerId()).
                set(CrawlerFailureRepository.URI, failure.job().datasource().uri());
    }

    @Override
    public Failure unmarshal(Record record) {
        PaginatedHttpJob job = PaginatedHttpJob.paginatedHttpJob(datasource(record), destination(record), lastCheckpointFor(record), moreUri(record), mappings);
        return Failure.failure(job, record.get(CrawlerFailureRepository.REASON));
    }
}