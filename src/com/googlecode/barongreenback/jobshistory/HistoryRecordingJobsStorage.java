package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.time.Seconds;
import com.googlecode.utterlyidle.jobs.InMemoryJobsStorage;
import com.googlecode.utterlyidle.jobs.Job;
import com.googlecode.utterlyidle.jobs.JobsStorage;
import com.googlecode.utterlyidle.jobs.RunningJob;

import java.util.Date;
import java.util.UUID;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;

public class HistoryRecordingJobsStorage implements JobsStorage {

    private InMemoryJobsStorage storage;
    private JobsHistoryRepository jobsHistoryRepository;

    public HistoryRecordingJobsStorage(InMemoryJobsStorage storage, JobsHistoryRepository jobsHistoryRepository) {
        this.storage = storage;
        this.jobsHistoryRepository = jobsHistoryRepository;
    }

    @Override
    public Option<Job> get(UUID uuid) {
        return storage.get(uuid);
    }

    @Override
    public Option<Job> put(Job job) {
        recordHistoryItem(job);
        return storage.put(job);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public Sequence<Job> jobs() {
        return storage.jobs();
    }

    private void recordHistoryItem(Job job) {
        jobsHistoryRepository.put(new JobHistoryItem(
                new JobId(job.id()),
                Seconds.between(job.created(), timestampFrom(job)),
                timestampFrom(job),
                actionFrom(job),
                messageFrom(job)));
    }

    private Option<String> messageFrom(Job job) {
        if (job instanceof RunningJob) {
            return none();
        }
        return some(job.response().map(Callables.asString()).getOrElse(job.request().toString()));
    }

    private String actionFrom(Job job) {
        if (job instanceof RunningJob) {
            return "started";
        }
        return job.status();
    }

    private Date timestampFrom(Job job) {
        return job.completed().getOrElse(job.started().getOrElse(job.created()));
    }
}