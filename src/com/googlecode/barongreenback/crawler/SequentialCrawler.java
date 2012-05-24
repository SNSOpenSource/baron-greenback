package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
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

import static com.googlecode.barongreenback.crawler.CheckPointStopper.extractCheckpoint;
import static com.googlecode.barongreenback.crawler.DuplicateRemover.ignoreAlias;
import static com.googlecode.barongreenback.shared.RecordDefinition.UNIQUE_FILTER;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Sequences.forwardOnly;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Uri.uri;
import static java.util.UUID.randomUUID;

public class SequentialCrawler implements Crawler {
    private final ModelRepository modelRepository;
    private final StringMappings mappings;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;
    private final CheckPointHandler checkPointHandler;

    public SequentialCrawler(ModelRepository modelRepository, StringMappings mappings, HttpClient httpClient, BaronGreenbackRecords records) {
        this.modelRepository = modelRepository;
        this.mappings = mappings;
        this.httpClient = httpClient;
        this.records = records;
        this.checkPointHandler = new CheckPointHandler(mappings, modelRepository);
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

        final String recordsToUpdate = update(crawler);
        Sequence<Keyword<?>> keywords = keywords(recordDefinition);

        updateView(recordsToUpdate, keywords);
        return put(recordsToUpdate, sequence(head).join(forwardOnly(recordIterator)), keywords);
    }

    private String update(Model crawler) {
        return crawler.get("update", String.class);
    }

    private Iterator<Record> startCrawl(PrintStream log, Model crawler, RecordDefinition recordDefinition) throws Exception {
        final Uri from = from(crawler);
        final String more = more(crawler);
        final Object lastCheckPoint = lastCheckPoint(crawler);
        return new CompositeCrawler(httpClient, log).crawl(from, more, lastCheckPoint, recordDefinition).iterator();
    }

    private Uri from(Model crawler) {
        return uri(crawler.get("from", String.class));
    }

    private String more(Model crawler) {
        return crawler.get("more", String.class);
    }

    private Object lastCheckPoint(Model crawler) throws Exception {
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        return convertFromString(checkpoint, checkpointType);
    }

    private RecordDefinition extractRecordDefinition(Model crawler) {
        return convert(crawler.get("record", Model.class));
    }

    private Model crawlerFor(UUID id) {
        return modelRepository.get(id).get().get("form", Model.class);
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

    private Sequence<Keyword<?>> keywords(RecordDefinition recordDefinition) {
        return RecordDefinition.allFields(recordDefinition).map(ignoreAlias());
    }

    private void updateView(final String recordName, Sequence<Keyword<?>> keywords) {
        if (find(modelRepository, recordName).isEmpty()) {
            modelRepository.set(randomUUID(), model().add(Views.ROOT, model().
                    add("name", recordName).
                    add("records", recordName).
                    add("query", "").
                    add("visible", true).
                    add("priority", "").
                    add("keywords", keywords.map(Views.asModel()).toList())));
        }
    }

    private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
        Class<?> aClass = checkpointType == null ? String.class : Class.forName(checkpointType);
        return mappings.get(aClass).toValue(checkpoint);
    }

    private Option<Object> getFirstCheckPoint(Record record) {
        return extractCheckpoint(record);
    }
}