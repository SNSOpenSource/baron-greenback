package com.googlecode.barongreenback.crawler.failure;

import com.googlecode.barongreenback.crawler.Failure;
import com.googlecode.lazyrecords.Record;

public interface FailureMarshaller {
    Record marshal(Failure failure);

    Failure unmarshal(Record record);
}
