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
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;

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

// Work in progress, does not support sub feeds yet
public class ConcurrentCrawler implements Crawler {
    private final ModelRepository modelRepository;
    private final StringMappings mappings;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;

    public ConcurrentCrawler(ModelRepository modelRepository, StringMappings mappings, HttpClient httpClient, BaronGreenbackRecords records) {
        this.modelRepository = modelRepository;
        this.mappings = mappings;
        this.httpClient = httpClient;
        this.records = records;
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


        HttpHandler client = new AuditHandler(httpClient, new PrintAuditor(log));
        DuplicateRemover duplicateRemover = new DuplicateRemover(new CheckPointStopper(lastCheckPoint, new UriFeeder(client, more)));
        Sequence<Record> records = duplicateRemover.get(uri, recordDefinition);
        Option<Record> head = records.headOption();
        if (head.isEmpty()) {
            return 0;
        }

        Option<Object> firstCheckPoint = getFirstCheckPoint(head.get());
        String newCheckPointType = getCheckPointType(firstCheckPoint);
        String newCheckPointValue = convertToString(firstCheckPoint);

        modelRepository.set(id, model().set("form", crawler.set("checkpoint", newCheckPointValue).set("checkpointType", newCheckPointType)));
        Sequence<Keyword<?>> keywords = RecordDefinition.allFields(recordDefinition).map(ignoreAlias());
        Definition definition = Definition.constructors.definition(update, keywords);
        if (find(modelRepository, update).isEmpty()) {
            modelRepository.set(randomUUID(), model().add(Views.ROOT, model().
                    add("name", update).
                    add("records", update).
                    add("query", "").
                    add("visible", true).
                    add("priority", "").
                    add("keywords", keywords.map(Views.asModel()).toList())));
        }
        Number updated = 0;
        for (Record record1 : records.cons(head.get())) {
            Sequence<Keyword<?>> unique = record1.keywords().filter(UNIQUE_FILTER);
            Number rows = this.records.value().put(definition, pair(using(unique).call(record1), record1));
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
