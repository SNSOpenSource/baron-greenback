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
                Sequence<Sequence<Record>> chunks = feeder.get(uri, recordDefinition).recursive(Sequences.<Record>splitAt(20));
                Number updated = 0;
                Option<Record> head = Option.none();
                for (Sequence<Record> lazyRecords : chunks) {
                    Sequence<Record> crawledRecords = lazyRecords.realise();
                    if (head.isEmpty()) {
                        head = crawledRecords.headOption();
                    }

                    crawledRecords.flatMapConcurrently(processAllSubFeeds()).realise();

                    for (Record currentRecord : crawledRecords.cons(head.get()).map(Record.functions.merge(uniqueKeys))) {
                        Sequence<Keyword<?>> unique = extractUniqueKeys(currentRecord);
                        Number rows = records.put(definition, pair(using(unique).call(currentRecord), currentRecord));
                        updated = Numbers.add(updated, rows);
                    }

                }

                return pair(updated, head.map(CheckPointStopper.extractCheckPoint()));
            } catch (LazyException e) {
                return handleError(uri, e.getCause());
            } catch (Exception e) {
                return handleError(uri, e);
            }
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

        private Callable1<Record, Sequence<Pair<Number, Option<Object>>>> processAllSubFeeds() {
            return new Callable1<Record, Sequence<Pair<Number, Option<Object>>>>() {
                @Override
                public Sequence<Pair<Number, Option<Object>>> call(Record record) throws Exception {
                    Sequence<Keyword<?>> subFeedKeys = record.keywords().
                            filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                            realise(); // Must Realise so we don't get concurrent modification as we add new fields to the record
                    if (subFeedKeys.isEmpty()) {
                        return one(nothing());
                    }
                    return subFeedKeys.mapConcurrently(eachSubFeedWith(record)).realise();
                }
            };
        }

        private Callable1<Keyword, Pair<Number, Option<Object>>> eachSubFeedWith(final Record record) {
            return new Callable1<Keyword, Pair<Number, Option<Object>>>() {
                public Pair<Number, Option<Object>> call(Keyword keyword) throws Exception {
                    Object value = record.get(keyword);
                    if (value == null) {
                        return nothing();
                    }
                    Uri subFeed = uri(value.toString());
                    Sequence<Keyword<?>> uniqueKeys = extractUniqueKeys(record);
                    Sequence<Object> values = record.getValuesFor(uniqueKeys);
                    Sequence<Pair<Keyword<?>, Object>> uniqueKeysAndValues = uniqueKeys.zip(values);

                    RecordDefinition subFeedDefinition = keyword.metadata().get(RECORD_DEFINITION);
                    return crawl(subFeed, subFeedDefinition, uniqueKeysAndValues);
                }
            };
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
