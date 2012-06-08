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
    private Document document;
    private final HttpDataSource dataSource;
    private final Definition destination;
    private Sequence<Record> merged;
    private Sequence<HttpJob> subfeedJobs;
    private final LogicalPredicate<Record> filter;

    public DocumentProcessor(Document document, HttpDataSource dataSource, Definition destination, LogicalPredicate<Record> filter) {
        this.document = document;
        this.dataSource = dataSource;
        this.destination = destination;
        this.filter = filter;
    }

    public DocumentProcessor(Document document, HttpDataSource dataSource, Definition destination, Object checkpoint) {
        this(document, dataSource, destination, Predicates.<Record>not(checkpointReached(checkpoint)));
    }

    public DocumentProcessor execute() {
        Sequence<Record> records = transformData(document, dataSource.definition());
        Sequence<Record> filtered = records.takeWhile(filter).realise();
        merged = mergePreviousUniqueIdentifiers(filtered, dataSource);
        subfeedJobs = subfeeds(filtered, destination);
        return this;
    }

    public Sequence<Record> merged() {
        return merged;
    }

    public Sequence<HttpJob> subfeedJobs() {
        return subfeedJobs;
    }
}