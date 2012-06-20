package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.w3c.dom.Document;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.checkpointReached;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.barongreenback.crawler.SubfeedJobCreator.createSubfeedJobs;
import static com.googlecode.lazyrecords.Record.constructors.record;

public class DocumentProcessor {
    private final Sequence<Record> merged;
    private final Sequence<HttpJob> subfeedJobs;

    public DocumentProcessor(Document document, HttpDataSource dataSource, Definition destination, LogicalPredicate<Record> filter) {
        Sequence<Record> records = transformData(document, dataSource.definition());
        Sequence<Record> filtered = records.takeWhile(filter).realise();
        merged = mergePreviousUniqueIdentifiers(filtered, dataSource);
        subfeedJobs = createSubfeedJobs(filtered, destination, dataSourceUniques(dataSource));
    }

    public DocumentProcessor(Document document, HttpDataSource dataSource, Definition destination, Object checkpoint) {
        this(document, dataSource, destination, Predicates.<Record>not(checkpointReached(checkpoint)));
    }

    public Sequence<Record> merged() {
        return merged;
    }

    public Sequence<HttpJob> subfeedJobs() {
        return subfeedJobs;
    }

    private static Sequence<Record> mergePreviousUniqueIdentifiers(Sequence<Record> records, final HttpDataSource dataSource) {
            return records.map(new Callable1<Record, Record>() {
                @Override
                public Record call(Record record) throws Exception {
                    return record(record.fields().join(dataSourceUniques(dataSource)));
                }
            });
    }

    private static Sequence<Pair<Keyword<?>, Object>> dataSourceUniques(HttpDataSource dataSource) {
        if (dataSource instanceof  SubfeedDatasource) {
            return ((SubfeedDatasource)dataSource).uniqueIdentifiers();
        } else {
            return Sequences.sequence();
        }

    }

}