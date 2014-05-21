package com.googlecode.barongreenback.jobshistory;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;

public interface JobsHistoryRepository {
    void put(JobHistoryItem item);

    Sequence<Record> find(String query);

    Number remove(String query);
}
