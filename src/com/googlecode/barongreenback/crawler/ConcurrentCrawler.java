package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.barongreenback.views.Views;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;

import java.io.PrintStream;
import java.util.UUID;

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
    private final CheckPointExtractor checkPointExtractor;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;

    public ConcurrentCrawler(ModelRepository modelRepository, StringMappings mappings, HttpClient httpClient, BaronGreenbackRecords records) {
        this.modelRepository = modelRepository;
        this.checkPointExtractor = new CheckPointExtractor(mappings);
        this.httpClient = httpClient;
        this.records = records;
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        final Model crawler = crawlerFor(id);
        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);
        updateView(crawler, keywords(recordDefinition));


        final Uri uri = uri(crawler.get("from", String.class));

        SubFeedCrawler subFeedCrawler = new SubFeedCrawler(records.value(), httpClient, crawler, checkPointExtractor, log);
        Pair<Number, Option<Object>> result = subFeedCrawler.crawl(uri, recordDefinition, Sequences.<Pair<Keyword<?>, Object>>empty());

        updateCheckPoint(id, crawler, result.second());

        return result.first();

    }

    public static class SubFeedCrawler {
        private final Records records;
        private final Model crawler;
        private final Object lastCheckPoint;
        private final String more;
        private final HttpClient client;

        public SubFeedCrawler(Records records, HttpClient httpClient, Model crawler, CheckPointExtractor checkPointExtractor, PrintStream log) throws Exception {
            this.records = records;
            this.crawler = crawler;
            this.more = crawler.get("more", String.class);
            this.lastCheckPoint = checkPointExtractor.lastCheckPointFor(crawler);
            this.client = new AuditHandler(httpClient, new PrintAuditor(log));
        }

        public Pair<Number, Option<Object>> crawl(Uri uri, RecordDefinition recordDefinition, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) throws Exception {
            DuplicateRemover duplicateRemover = new DuplicateRemover(new CheckPointStopper(lastCheckPoint, new UriFeeder(client, more)));

            Sequence<Record> records = duplicateRemover.get(uri, recordDefinition);
            Option<Record> head = records.headOption();
            if (head.isEmpty()) {
                return pair((Number) 0, Option.none());
            }

            Definition definition = definition(crawler, recordDefinition);
            Number updated = 0;
            for (Record record1 : records.cons(head.get())) {
                Sequence<Keyword<?>> unique = record1.keywords().filter(UNIQUE_FILTER);
                Number rows = this.records.put(definition, pair(using(unique).call(record1), record1));
                updated = Numbers.add(updated, rows);
            }
            return pair(updated, CheckPointStopper.extractCheckpoint(head.get()));
        }
    }

    public static class CheckPointExtractor{
        private final StringMappings mappings;

        public CheckPointExtractor(StringMappings mappings) {
            this.mappings = mappings;
        }

        public Object lastCheckPointFor(Model crawler) throws Exception {
            final String checkpoint = crawler.get("checkpoint", String.class);
            final String checkpointType = crawler.get("checkpointType", String.class);
            return convertFromString(checkpoint, checkpointType);
        }

        private Object convertFromString(String checkpoint, String checkpointType) throws Exception {
            Class<?> aClass = checkpointType == null ? String.class : Class.forName(checkpointType);
            return mappings.get(aClass).toValue(checkpoint);
        }

        private String convertToString(Object checkpoint) {
            return mapAsString(checkpoint);
        }

        private Model updateCheckpoint(Model crawler, Object checkpoint) {
            return model().set("form", crawler.set("checkpoint", convertToString(checkpoint)).set("checkpointType", getCheckPointType(checkpoint)));
        }

        private String getCheckPointType(Object checkpoint) {
            if(checkpoint == null) return String.class.getName();
            return checkpoint.getClass().getName();
        }

        private String mapAsString(Object instance) {
            if(instance == null) return "";
            return mappings.toString(instance.getClass(), instance);
        }
    }


    private RecordDefinition extractRecordDefinition(Model crawler) {
        final Model record = crawler.get("record", Model.class);
        return convert(record);
    }


    private Model crawlerFor(UUID id) {
        return modelRepository.get(id).get().get("form", Model.class);
    }

    private static Definition definition(Model crawler, RecordDefinition recordDefinition) {
        return Definition.constructors.definition(update(crawler), keywords(recordDefinition));
    }

    private void updateCheckPoint(UUID id, Model crawler, Option<Object> checkpoint) {
        if (checkpoint.isEmpty()) {
            modelRepository.set(id, checkPointExtractor.updateCheckpoint(crawler, checkpoint.value()));
        }
    }

    private static Sequence<Keyword<?>> keywords(RecordDefinition recordDefinition) {
        return RecordDefinition.allFields(recordDefinition).map(ignoreAlias());
    }

    private void updateView(Model crawler, Sequence<Keyword<?>> keywords) {
        final String update = update(crawler);
        if (find(modelRepository, update).isEmpty()) {
            modelRepository.set(randomUUID(), model().add(Views.ROOT, model().
                    add("name", update).
                    add("records", update).
                    add("query", "").
                    add("visible", true).
                    add("priority", "").
                    add("keywords", keywords.map(Views.asModel()).toList())));
        }
    }

    private static String update(Model crawler) {
        return crawler.get("update", String.class);
    }




}
