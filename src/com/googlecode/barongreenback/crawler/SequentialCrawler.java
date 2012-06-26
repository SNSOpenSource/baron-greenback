package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.handlers.HttpClient;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.extractCheckpoint;
import static com.googlecode.totallylazy.Sequences.forwardOnly;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SequentialCrawler extends AbstractCrawler {
    private final StringMappings mappings;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;
    private final CheckPointHandler checkPointHandler;

    public SequentialCrawler(ModelRepository modelRepository, StringMappings mappings, HttpClient httpClient, BaronGreenbackRecords records, CheckPointHandler checkPointHandler1) {
        super(modelRepository);
        this.mappings = mappings;
        this.httpClient = httpClient;
        this.records = records;
        this.checkPointHandler = checkPointHandler1;
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        final Model crawler = crawlerFor(id);
        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);

        Iterator<Record> recordIterator = startCrawl(log, crawler, recordDefinition);
        if (!recordIterator.hasNext()) {
            return 0;
        }

        Record head = recordIterator.next();
        checkPointHandler.updateCheckPoint(id, crawler, getFirstCheckPoint(head));

        Sequence<Keyword<?>> keywords = keywords(recordDefinition);
        updateView(crawler, keywords);
        return put(update(crawler), sequence(head).join(forwardOnly(recordIterator)), keywords);
    }

    private Iterator<Record> startCrawl(PrintStream log, Model crawler, RecordDefinition recordDefinition) throws Exception {
        final Uri from = from(crawler);
        final String more = more(crawler);
        final Object lastCheckPoint = lastCheckPoint(crawler);
        return new CompositeCrawler(httpClient, log).crawl(from, more, lastCheckPoint, recordDefinition).iterator();
    }

    private Object lastCheckPoint(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    private Number put(final String recordName, final Sequence<Record> recordsToAdd, final Sequence<Keyword<?>> keywords1) {
        Definition definition = Definition.constructors.definition(recordName, keywords1);
        return new DataWriter(records.value()).writeUnique(definition, recordsToAdd);
    }

    private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
        Class<?> aClass = checkpointType == null ? String.class : Class.forName(checkpointType);
        return mappings.get(aClass).toValue(checkpoint);
    }

    private Option<Object> getFirstCheckPoint(Record record) {
        return extractCheckpoint(record);
    }
}