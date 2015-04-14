package com.googlecode.barongreenback.jobshistory;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;

import java.util.Date;

import static com.googlecode.lazyrecords.Grammar.keyword;

public interface JobHistoryItemDefinition extends Definition {
    JobHistoryItemDefinition jobsHistory = constructors.definition(JobHistoryItemDefinition.class, "jobsHistory");
    Keyword<Date> timestamp = keyword("timestamp", Date.class);
    Keyword<Long> elapsedTime = keyword("elapsedTime", Long.class);
    Keyword<String> action = keyword("action", String.class);
    Keyword<JobId> jobId = keyword("jobId", JobId.class);
    Keyword<String> message = keyword("message", String.class);
}
