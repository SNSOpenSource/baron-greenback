package com.sky.sns.barongreenback.crawler.failures;

import com.googlecode.lazyrecords.Record;

public interface FailureMarshaller {
    Record marshal(Failure failure);

    Failure unmarshal(Record record);
}
