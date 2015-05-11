package sky.sns.barongreenback.crawler;

import sky.sns.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;

public interface Feeder<T> {
    Sequence<Record> get(T source, RecordDefinition definition) throws Exception;
}
