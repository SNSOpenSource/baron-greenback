package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.CheckpointHandler;
import com.googlecode.barongreenback.crawler.CrawlerRepository;
import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.barongreenback.crawler.MasterPaginatedHttpJob;
import com.googlecode.barongreenback.crawler.PaginatedHttpJob;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class MasterPaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public MasterPaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, StringMappings mappings) {
        super(crawlerRepository, checkpointHandler);
        this.mappings = mappings;
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
        MasterPaginatedHttpJob job = MasterPaginatedHttpJob.masterPaginatedHttpJob(datasource(record), destination(record), lastCheckpointFor(record), moreUri(record), mappings);
        return Failure.failure(job, record.get(CrawlerFailureRepository.reason));
    }
}