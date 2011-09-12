package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import java.net.URL;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.RecordMethods.merge;
import static com.googlecode.totallylazy.records.xml.Xml.selectContents;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static java.lang.Boolean.TRUE;


public class Crawler {
    public static final Keyword<Boolean> CHECKPOINT = Keywords.keyword("checkpoint", Boolean.class);
    private final HttpClient httpClient;

    public Crawler() {
        httpClient = new ClientHttpHandler();
    }

    public Crawler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public XmlRecords records(Document document) throws Exception {
        return new XmlRecords(document);
    }

    private Document document(URL url) throws Exception {
        Response response = httpClient.handle(get(url.toString()).build());
        String xml = new String(response.bytes());
        return Xml.document(xml);
    }

    public Pair<? extends Keyword<String>, Sequence<Record>> crawl(URL url, RecordDefinition recordDefinition, Keyword<String> moreSelector, Keyword<String> checkpoint) throws Exception {
        Document document = document(url);
        Pair<Sequence<Record>, Boolean> newRecordsOnCurrentPageAndMoreIndicator = crawl(document, recordDefinition, checkpoint);
        Sequence<Record> newRecordsOnCurrentPage = newRecordsOnCurrentPageAndMoreIndicator.first();
        Boolean moreIndicator = newRecordsOnCurrentPageAndMoreIndicator.second();

        String newCheckpoint = evaluateNewCheckpoint(newRecordsOnCurrentPage, checkpoint);

        if (moreIndicator && moreResultsLink(moreSelector, document)) {
            Sequence<Record> newRecordsOnPreviousPages = crawl(url(moreLink(moreSelector, document)), recordDefinition, moreSelector, checkpoint).second();
            return Pair.pair(keyword(newCheckpoint, String.class), newRecordsOnCurrentPage.join(newRecordsOnPreviousPages));
        }

        return Pair.pair(keyword(newCheckpoint, String.class), newRecordsOnCurrentPage);
    }

    private String evaluateNewCheckpoint(Sequence<Record> recordsSoFar, Keyword<String> oldCheckpoint) {
        if(recordsSoFar.isEmpty()) return oldCheckpoint.name();
        Option<Keyword> checkpoint = recordsSoFar.first().keywords().find(checkpoint());
        if(checkpoint.isEmpty()) return oldCheckpoint.name();
        return recordsSoFar.first().get(checkpoint.get()).toString();
    }

    private String moreLink(Keyword<String> moreSelector, Document document) {
        return selectContents(document, moreSelector.name());
    }

    private boolean moreResultsLink(Keyword<String> more, Document document) {
        return !Strings.isEmpty(more.name()) && !Strings.isEmpty(moreLink(more, document));
    }

    public Pair<Sequence<Record>, Boolean> crawl(URL url, RecordDefinition recordDefinition, Keyword<String> checkpoint) throws Exception {
        Document document = document(url);
        return crawl(document, recordDefinition, checkpoint);
    }

    public Pair<Sequence<Record>, Boolean> crawl(Document document, RecordDefinition recordDefinition, Keyword<String> checkpoint) throws Exception {
        XmlRecords xmlRecords = records(document);
        Sequence<Keyword> allFields = recordDefinition.fields();

        xmlRecords.define(recordDefinition.recordName(), allFields.toArray(Keyword.class));

        Sequence<Record> results = xmlRecords.get(recordDefinition.recordName());
        Sequence<Record> resultsAfterCheckpoint  = results.takeWhile(not(checkpointReached(checkpoint)));

        Sequence<Record> records = allFields.filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                fold(resultsAfterCheckpoint, crawlSubFeeds()).realise();
        boolean hasMore = results.equals(resultsAfterCheckpoint);
        return Pair.pair(records, hasMore);

    }

    private Predicate<? super Record> checkpointReached(final Keyword<String> checkpointValue) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                Sequence<Keyword> checkpoints = record.keywords().filter(checkpoint());
                return checkpoints.exists(checkpointValue(record, checkpointValue));
            }
        };
    }

    private Predicate<? super Keyword> checkpointValue(final Record record, final Keyword<String> checkpoint) {
        return new Predicate<Keyword>() {
            public boolean matches(Keyword keyword) {
                return record.get(keyword).toString().equals(checkpoint.name());
            }
        };
    }

    private Predicate<? super Keyword> checkpoint() {
        return new Predicate<Keyword>() {
            public boolean matches(Keyword keyword) {
                return TRUE.equals(keyword.metadata().get(CHECKPOINT));
            }
        };
    }

    private Callable2<Sequence<Record>, Keyword, Sequence<Record>> crawlSubFeeds() {
        return new Callable2<Sequence<Record>, Keyword, Sequence<Record>>() {
            public Sequence<Record> call(Sequence<Record> records, Keyword keyword) throws Exception {
                return records.flatMap(crawl(keyword));
            }
        };
    }

    private Callable1<Record, Iterable<Record>> crawl(final Keyword sourceUrl) {
        return new Callable1<Record, Iterable<Record>>() {
            public Iterable<Record> call(Record currentRecord) throws Exception {
                try {
                    URL subFeed = url(currentRecord.get(sourceUrl).toString());
                    RecordDefinition subDefinitions = sourceUrl.metadata().get(RECORD_DEFINITION);
                    return crawl(subFeed, subDefinitions, keyword("", String.class)).first().
                            map(merge(currentRecord));
                } catch (Exception e) {
                    return Sequences.sequence(currentRecord);
                }
            }
        };
    }

}
