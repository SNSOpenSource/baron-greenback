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
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.utterlyidle.handlers.HttpClient;

import java.io.PrintStream;
import java.util.UUID;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.extractCheckpoint;
import static com.googlecode.barongreenback.crawler.DuplicateRemover.ignoreAlias;
import static com.googlecode.barongreenback.shared.RecordDefinition.UNIQUE_FILTER;
import static com.googlecode.barongreenback.shared.RecordDefinition.convert;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Callables.toClass;
import static com.googlecode.totallylazy.Pair.pair;
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
        final Model model = modelRepository.get(id).get();
        final Model crawler = model.get("form", Model.class);
        final String from = crawler.get("from", String.class);
        final String update = crawler.get("update", String.class);
        final String more = crawler.get("more", String.class);
        final String checkpoint = crawler.get("checkpoint", String.class);
        final String checkpointType = crawler.get("checkpointType", String.class);
        final Model record = crawler.get("record", Model.class);
        final RecordDefinition recordDefinition = convert(record);

        Uri uri = uri(from);
        Object lastCheckPoint = convertFromString(checkpoint, checkpointType);

        Sequence<Record> records = new CompositeCrawler(httpClient, log).crawl(uri, more, lastCheckPoint, recordDefinition);
        Option<Record> head = records.headOption();
        if (head.isEmpty()) {
            return 0;
        }

        checkPointHandler.updateCheckPoint(id, crawler, getFirstCheckPoint(head.get()));

        return put(update, recordDefinition, records.cons(head.get()));
    }

    private Number put(final String recordName, RecordDefinition recordDefinition, final Sequence<Record> recordsToAdd) {
        Sequence<Keyword<?>> keywords = RecordDefinition.allFields(recordDefinition).map(ignoreAlias());
        Definition definition = Definition.constructors.definition(recordName, keywords);
        if (find(modelRepository, recordName).isEmpty()) {
            modelRepository.set(randomUUID(), model().add(Views.ROOT, model().
                    add("name", recordName).
                    add("records", recordName).
                    add("query", "").
                    add("visible", true).
                    add("priority", "").
                    add("keywords", keywords.map(Views.asModel()).toList())));
        }
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

    private Option<Object> getFirstCheckPoint(Record record) {
        return extractCheckpoint(record);
    }

    private Callable1<? super Object, String> mapAsString() {
        return new Callable1<Object, String>() {
            public String call(Object instance) throws Exception {
                return mappings.toString(instance.getClass(), instance);
            }
        };
    }

    private String convertToString(Option<Object> checkPoint) {

        return checkPoint.map(mapAsString()).getOrElse("");
    }

    private String getCheckPointType(Option<Object> checkpoint) {
        return checkpoint.map(toClass()).
                map(className()).
                getOrElse(String.class.getName());
    }

    private static Callable1<? super Class, String> className() {
        return new Callable1<Class, String>() {
            public String call(Class aClass) throws Exception {
                return aClass.getName();
            }
        };
    }



}
