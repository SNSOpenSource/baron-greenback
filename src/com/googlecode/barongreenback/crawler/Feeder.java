package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Record;

public interface Feeder<T> {
    Sequence<Record> get(T source, RecordDefinition definition) throws Exception;
}
