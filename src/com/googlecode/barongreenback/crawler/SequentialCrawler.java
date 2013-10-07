package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.persistence.BaronGreenbackStringMappings;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.utterlyidle.handlers.HttpClient;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CheckpointStopper.extractCheckpoint;
import static com.googlecode.barongreenback.shared.RecordDefinition.UNIQUE_FILTER;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Sequences.forwardOnly;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SequentialCrawler extends AbstractCrawler {
    private final StringMappings mappings;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;
    private final CheckpointHandler checkpointHandler;
    private final PrintStream log;

    public SequentialCrawler(CrawlerRepository crawlerRepository, BaronGreenbackStringMappings mappings, CrawlerHttpClient httpClient, BaronGreenbackRecords records, CheckpointHandler checkpointHandler, PrintStream log) {
        super(crawlerRepository);
        this.mappings = mappings.value();
        this.httpClient = httpClient;
        this.records = records;
        this.checkpointHandler = checkpointHandler;
        this.log = log;
    }

    @Override
    public Number crawl(UUID id) throws Exception {
        final Model crawler = crawlerFor(id);
        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);
        Sequence<Keyword<?>> keywords = keywords(recordDefinition);

        Iterator<Record> recordIterator = startCrawl(log, crawler, recordDefinition);
        if (!recordIterator.hasNext()) {
            return 0;
        }

        Record head = recordIterator.next();
        checkpointHandler.updateCheckpoint(id, crawler, getFirstCheckpoint(head));

        return put(update(crawler), sequence(head).join(forwardOnly(recordIterator)), keywords);
    }

    private Iterator<Record> startCrawl(PrintStream log, Model crawler, RecordDefinition recordDefinition) throws Exception {
        final Uri from = from(crawler);
        final String more = more(crawler);
        final Object lastCheckpoint = lastCheckpoint(crawler);
        return new CompositeCrawler(httpClient, log).crawl(from, more, lastCheckpoint, recordDefinition).iterator();
    }

    private Object lastCheckpoint(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    private Number put(final String recordName, final Sequence<Record> recordsToAdd, final Sequence<Keyword<?>> keywords1) {
        Definition definition = Definition.constructors.definition(recordName, keywords1);
        Number updated = 0;
        for (Record record : recordsToAdd) {
            Sequence<Keyword<?>> unique = record.keywords().filter(UNIQUE_FILTER);
            Number rows = records.value().put(definition, Record.methods.update(using(unique), record));
            updated = Numbers.add(updated, rows);
        }
        return updated;
    }

    private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
        Class<?> aClass = checkpointType == null ? String.class : Class.forName(checkpointType);
        return mappings.get(aClass).toValue(checkpoint);
    }

    private Option<Object> getFirstCheckpoint(Record record) {
        return extractCheckpoint(record);
    }
}