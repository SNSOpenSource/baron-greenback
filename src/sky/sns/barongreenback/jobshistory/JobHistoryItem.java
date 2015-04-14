package com.googlecode.barongreenback.jobshistory;

import com.googlecode.totallylazy.Option;

import java.util.Date;

public class JobHistoryItem {
    private JobId jobId;
    private long elapsedTimeInSeconds;
    private Date timestamp;
    private String action;
    private Option<String> message;

    public JobHistoryItem(JobId jobId, long elapsedTimeInSeconds, Date timestamp, String action, Option<String> message) {
        this.jobId = jobId;
        this.elapsedTimeInSeconds = elapsedTimeInSeconds;
        this.timestamp = timestamp;
        this.action = action;
        this.message = message;
    }

    public JobId getJobId() {
        return jobId;
    }

    public long getElapsedTimeInSeconds() {
        return elapsedTimeInSeconds;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public Option<String> getMessage() {
        return message;
    }
}
