package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.w3c.dom.Document;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.checkpointReached;
import static com.googlecode.barongreenback.crawler.DataTransformer.transformData;
import static com.googlecode.barongreenback.crawler.Subfeeder2.mergePreviousUniqueIdentifiers;
import static com.googlecode.barongreenback.crawler.Subfeeder2.subfeeds;

public class DocumentProcessor {
    private final Sequence<Record> merged;
    private final Sequence<HttpJob> subfeedJobs;

    public DocumentProcessor(Document document, HttpDataSource dataSource, Definition destination, LogicalPredicate<Record> filter) {
        Sequence<Record> records = transformData(document, dataSource.definition());
        Sequence<Record> filtered = records.takeWhile(filter).realise();
        merged = mergePreviousUniqueIdentifiers(filtered, dataSource);
        subfeedJobs = subfeeds(filtered, destination);
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
}