package sky.sns.barongreenback.crawler.failures;

import sky.sns.barongreenback.crawler.CheckpointHandler;
import sky.sns.barongreenback.crawler.CrawlerRepository;
import sky.sns.barongreenback.crawler.HttpVisitedFactory;
import sky.sns.barongreenback.crawler.jobs.MasterPaginatedHttpJob;
import sky.sns.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.time.Clock;

import static sky.sns.barongreenback.crawler.failures.FailureRepository.DURATION;

public class MasterPaginatedJobFailureMarshaller extends AbstractFailureMarshaller {
    private final StringMappings mappings;

    public MasterPaginatedJobFailureMarshaller(CrawlerRepository crawlerRepository, CheckpointHandler checkpointHandler, BaronGreenbackStringMappings mappings, HttpVisitedFactory visitedFactory, Clock clock) {
        super(crawlerRepository, checkpointHandler, visitedFactory, clock);
        this.mappings = mappings.value();
    }

    @Override
    public Failure unmarshal(Record record) {
        MasterPaginatedHttpJob job = MasterPaginatedHttpJob.masterPaginatedHttpJob(crawlerId(record), dataSource(record), destination(record), lastCheckpointFor(record), moreUri(record), visited, clock);
        return Failure.failure(job, record.get(FailureRepository.REASON), record.get(DURATION));
    }
}