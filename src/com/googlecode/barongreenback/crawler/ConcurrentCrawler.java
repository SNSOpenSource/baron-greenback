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
import static com.googlecode.barongreenback.shared.RecordDefinition.*;
import static com.googlecode.barongreenback.views.Views.find;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Uri.uri;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

// Work in progress, does not support sub feeds yet
public class ConcurrentCrawler implements Crawler {
    private final ModelRepository modelRepository;
    private final CheckPointHandler checkPointHandler;
    private final HttpClient httpClient;
    private final BaronGreenbackRecords records;

    public ConcurrentCrawler(ModelRepository modelRepository, StringMappings mappings, HttpClient httpClient, BaronGreenbackRecords records) {
        this.modelRepository = modelRepository;
        this.checkPointHandler = new CheckPointHandler(mappings);
        this.httpClient = httpClient;
        this.records = records;
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        final Model crawler = crawlerFor(id);
        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);
        updateView(crawler, keywords(recordDefinition));

        SubFeedCrawler subFeedCrawler = new SubFeedCrawler(records.value(), httpClient, crawler, checkPointHandler, log, definition(crawler, recordDefinition));

        final Uri uri = uri(crawler.get("from", String.class));
        Pair<Number, Option<Object>> result = subFeedCrawler.crawl(uri, recordDefinition, Sequences.<Pair<Keyword<?>, Object>>empty());

        updateCheckPoint(id, crawler, result.second());

        return result.first();

    }

    public static class SubFeedCrawler {
        private final Records records;
        private final Object lastCheckPoint;
        private final String more;
        private final HttpClient client;
        private final PrintStream log;
        private final Definition definition;

        public SubFeedCrawler(Records records, HttpClient httpClient, Model crawler, CheckPointHandler checkPointExtractor, PrintStream log, Definition definition) throws Exception {
            this.records = records;
            this.log = log;
            this.definition = definition;
            this.more = crawler.get("more", String.class);
            this.lastCheckPoint = checkPointExtractor.lastCheckPointFor(crawler);
            this.client = new AuditHandler(httpClient, new PrintAuditor(log));
        }

        public Pair<Number, Option<Object>> crawl(Uri uri, RecordDefinition recordDefinition, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) throws Exception {
            try {
                Feeder<Uri> feeder = new DuplicateRemover(new CheckPointStopper(lastCheckPoint, new UriFeeder(client, more)));
                Sequence<Record> crawledRecords = feeder.get(uri, recordDefinition);

                Option<Record> head = crawledRecords.headOption();
                if (head.isEmpty()) {
                    return nothing();
                }

                Number updated = 0;
                for (Record currentRecord : crawledRecords.cons(head.get())) {
                    Number rows = putRecord(uniqueKeys, currentRecord);
                    updated = Numbers.add(updated, rows);

                    Sequence<Pair<Keyword<?>, Object>> accumulatedUniqueKeys = uniqueKeysAndValues(currentRecord).join(uniqueKeys);
                    Sequence<Pair<Number, Option<Object>>> result = processSubFeeds(currentRecord, accumulatedUniqueKeys);
                }

                Pair<Number, Option<Object>> pair = pair(updated, CheckPointStopper.extractCheckpoint(head.get()));
                System.out.printf("Feed %s returned %s\n", uri, pair.first());
                return pair;
            } catch (LazyException e) {
                return handleError(uri, e.getCause());
            } catch (Exception e) {
                return handleError(uri, e);
            }
        }

        private Number putRecord(Sequence<Pair<Keyword<?>, Object>> uniqueKeys, Record currentRecord) {
            return records.put(definition, Record.methods.update(using(uniqueKeys.map(Callables.<Keyword<?>>first())), addUniqueKeysTo(currentRecord, uniqueKeys)));
        }

        private Record addUniqueKeysTo(Record currentRecord, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
            return Record.constructors.record(uniqueKeys.join(currentRecord.fields()));
        }

        private Sequence<Pair<Keyword<?>, Object>> uniqueKeysAndValues(Record currentRecord) {
            return currentRecord.fields().filter(where(Callables.<Keyword<?>>first(), UNIQUE_FILTER));
        }

        private Pair<Number, Option<Object>> handleError(Uri subFeed, Throwable e) {
            log.println(format("Failed to GET %s because of %s", subFeed, e));
            return nothing();
        }

        private Pair<Number, Option<Object>> nothing() {
            return pair((Number) 0, Option.none());
        }

        private Sequence<Keyword<?>> extractUniqueKeys(Record currentRecord) {
            return currentRecord.keywords().filter(UNIQUE_FILTER);
        }

        private Sequence<Pair<Number, Option<Object>>> processSubFeeds(Record record, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
            Sequence<Keyword<?>> subFeedKeys = record.keywords().
                    filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                    realise(); // Must Realise so we don't get concurrent modification as we add new fields to the record
            if (subFeedKeys.isEmpty()) {
                return one(nothing());
            }
            return subFeedKeys.mapConcurrently(processSubFeed(record, uniqueKeys)).realise();
        }

        private Callable1<Keyword, Pair<Number, Option<Object>>> processSubFeed(final Record record, final Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
            return new Callable1<Keyword, Pair<Number, Option<Object>>>() {
                public Pair<Number, Option<Object>> call(Keyword keyword) throws Exception {
                    return processSubFeed(keyword, record, uniqueKeys);
                }
            };
        }

        private Pair<Number, Option<Object>> processSubFeed(Keyword keyword, Record record, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) throws Exception {
            Object value = record.get(keyword);
            if (value == null) {
                return nothing();
            }
            Uri subFeed = uri(value.toString());

            RecordDefinition subFeedDefinition = keyword.metadata().get(RECORD_DEFINITION);
            return crawl(subFeed, subFeedDefinition, uniqueKeys);
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
            modelRepository.set(id, checkPointHandler.addCheckpoint(crawler, checkpoint.value()));
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
