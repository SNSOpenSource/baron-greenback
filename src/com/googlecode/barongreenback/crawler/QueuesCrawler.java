package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.persistence.BaronGreenbackRecords;
import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.funclate.Model;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Uri;
import com.googlecode.utterlyidle.*;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.*;

import static com.googlecode.barongreenback.crawler.CheckPointStopper.checkpointReached;
import static com.googlecode.barongreenback.crawler.ConcurrentCrawler.SubFeedCrawler.addUniqueKeysTo;
import static com.googlecode.barongreenback.crawler.ConcurrentCrawler.SubFeedCrawler.uniqueKeysAndValues;
import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.barongreenback.shared.RecordDefinition.UNIQUE_FILTER;
import static com.googlecode.lazyrecords.Keywords.metadata;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Uri.uri;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.Xml.selectContents;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.ResponseBuilder.response;
import static com.googlecode.utterlyidle.handlers.Handlers.asFunction;

public class QueuesCrawler extends AbstractCrawler {
    private final ExecutorService httpHandlers;
    private final ExecutorService dataMappers;
    private final ExecutorService writers;
    private final Function1<Request, Response> httpHandler;
    private final Records records;
    private final CheckPointHandler checkPointExtractor;
    private final LinkedBlockingDeque<Pair<Request,Response>> retryQueue = new LinkedBlockingDeque<Pair<Request, Response>>();

    public QueuesCrawler(final ModelRepository modelRepository, final HttpClient httpClient, final BaronGreenbackRecords records, final CheckPointHandler checkPointExtractor) {
        super(modelRepository);
        this.checkPointExtractor = checkPointExtractor;
        httpHandler = asFunction(httpClient);
        httpHandlers = Executors.newFixedThreadPool(50);
        dataMappers = Executors.newCachedThreadPool();
        writers = Executors.newSingleThreadExecutor();
        this.records = records.value();
    }

    @Override
    public Number crawl(UUID id, PrintStream log) throws Exception {
        Model crawler = crawlerFor(id);
        Definition source = sourceDefinition(crawler);
        Definition destination = destinationDefinition(crawler);

        final RecordDefinition recordDefinition = extractRecordDefinition(crawler);
        updateView(crawler, keywords(recordDefinition));

        Object lastCheckPoint = checkPointExtractor.lastCheckPointFor(crawler);

        crawl(requestFor(crawler), source, destination, more(crawler), lastCheckPoint, Sequences.<Pair<Keyword<?>, Object>>empty());
        return -1;
    }

    private Request requestFor(Model crawler) {
        return RequestBuilder.get(from(crawler)).build();
    }

    private Future<?> crawl(Request request, Definition source, Definition destination, String moreXpath, Object lastCheckPoint, final Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        return submit(httpHandlers, get(request).then(FailureHandler.captureFailures(request, retryQueue).then(
                submit(dataMappers, DataExtractor.extractData(source).then(submit(writers, DataWriter.simpleWrite(destination, records)))))));

        //extractData(source, destination, moreXpath, lastCheckPoint, uniqueKeys).then(queueSubFeeds(destination)
    }

    private void queueSubFeeds(Sequence<Record> records, Definition destination) {
        for (Record record : records) {
            Sequence<Keyword<?>> subFeedKeys = record.keywords().
                    filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                    realise(); // Must Realise so we don't get concurrent modification as we add new fields to the record

            for (Keyword<?> subFeedKey : subFeedKeys) {
                Object value = record.get(subFeedKey);
                if (value == null) {
                    continue;
                }

                Uri subFeed = uri(value.toString());
                Definition newSource = subFeedKey.metadata().get(RECORD_DEFINITION).definition();
                crawl(RequestBuilder.get(subFeed).build(), newSource, destination, null, null, uniqueKeysAndValues(record));
            }
        }
    }

    private Future<?> submit(ExecutorService executorService, Runnable runnable) {
        return executorService.submit(runnable);
    }

    private Function<Response> get(Request request) {
        return httpHandler.deferApply(request);
    }

    private Sequence<Record> extractData(Response response, final Definition source, final Definition destination, final String moreXpath, final Object lastCheckPoint, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        try {
            Document document = loadDocument(response);
            Sequence<Record> allRecords = new DocumentFeeder().get(document, source).map(DataExtractor.copy()).realise();
            Sequence<Record> newRecords = allRecords.takeWhile(not(checkpointReached(lastCheckPoint))).realise();
            if (newRecords.size().equals(allRecords.size())) {
                more(document, moreXpath, source, destination, lastCheckPoint, uniqueKeys);
            }
            return newRecords;
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private Document loadDocument(Response response) {
        return document(response.entity().toString());
    }

    private void more(Document document, String moreXpath, Definition source, Definition destination, Object lastCheckPoint, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        if (Strings.isEmpty(moreXpath)) return;
        String uri = selectContents(document, moreXpath);
        if (Strings.isEmpty(uri)) return;
        crawl(RequestBuilder.get(uri(uri)).build(), source, destination, moreXpath, lastCheckPoint, uniqueKeys);
    }

    private Number write(Sequence<Record> dataToAdd, Definition definition, Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        for (Record currentRecord : dataToAdd) {
            Record combinedRecord = addUniqueKeysTo(currentRecord, uniqueKeys);
            Sequence<Keyword<?>> unique = combinedRecord.keywords().filter(UNIQUE_FILTER);
            records.put(definition, Record.methods.update(using(unique), combinedRecord));
        }
        return -1;
    }


    private Function1<Response, Sequence<Record>> extractData(final Definition source, final Definition destination, final String moreXpath, final Object lastCheckPoint, final Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        return new Function1<Response, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Response response) throws Exception {
                return extractData(response, source, destination, moreXpath, lastCheckPoint, uniqueKeys);
            }
        };
    }


    private Function1<Sequence<Record>, Number> write(final Definition definition, final Sequence<Pair<Keyword<?>, Object>> uniqueKeys) {
        return new Function1<Sequence<Record>, Number>() {
            @Override
            public Number call(Sequence<Record> dataToAdd) throws Exception {
                return write(dataToAdd, definition, uniqueKeys);
            }
        };
    }


    private <T> Function1<T, Future<?>> submit(final ExecutorService executorService, final Function1<T, ?> then) {
        return new Function1<T, Future<?>>() {
            @Override
            public Future<?> call(T result) throws Exception {
                return executorService.submit((Runnable) then.deferApply(result));
            }
        };
    }

    private Function1<Sequence<Record>, Sequence<Record>> queueSubFeeds(final Definition destination) {
        return new Function1<Sequence<Record>, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Sequence<Record> records) throws Exception {
                queueSubFeeds(records, destination);
                return records;
            }
        };
    }
}
