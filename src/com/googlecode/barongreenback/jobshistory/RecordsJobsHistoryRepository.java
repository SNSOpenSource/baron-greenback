package com.googlecode.barongreenback.jobshistory;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.search.PredicateBuilder;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.action;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.elapsedTime;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.jobId;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.jobsHistory;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.message;
import static com.googlecode.barongreenback.jobshistory.JobHistoryItemDefinition.timestamp;
import static com.googlecode.totallylazy.Either.right;

public class RecordsJobsHistoryRepository implements JobsHistoryRepository {

    private Records records;
    private final PredicateBuilder predicateBuilder;

    public RecordsJobsHistoryRepository(BaronGreenbackRecords records, PredicateBuilder predicateBuilder) {
        this.predicateBuilder = predicateBuilder;
        this.records = records.value();
    }

    @Override
    public void put(JobHistoryItem item) {
        records.add(jobsHistory, toRecord().apply(item));
    }

    @Override
    public Either<String, Sequence<Record>> find(String query) {
        final Either<String, Predicate<Record>> invalidQueryOrPredicate = predicateBuilder.build(query, jobsHistory.fields());
        if (invalidQueryOrPredicate.isLeft()) return Either.left(invalidQueryOrPredicate.left());

        return right(records.get(jobsHistory).filter(invalidQueryOrPredicate.right()));
    }

    @Override
    public Number remove(String query) {
        final Predicate<Record> filter = predicateBuilder.build(query, jobsHistory.fields()).right();
        return records.remove(jobsHistory, filter);
    }

    public static Mapper<JobHistoryItem, Record> toRecord() {
        return new Mapper<JobHistoryItem, Record>() {
            @Override
            public Record call(JobHistoryItem item) throws Exception {
                return Record.constructors.record()
                        .set(jobId, item.getJobId())
                        .set(action, item.getAction())
                        .set(timestamp, item.getTimestamp())
                        .set(elapsedTime, item.getElapsedTimeInSeconds())
                        .set(message, item.getMessage().getOrNull());
            }
        };
    }
}
