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
import com.googlecode.totallylazy.records.ImmutableKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.totallylazy.records.xml.mappings.DateMapping;
import com.googlecode.totallylazy.records.xml.mappings.Mappings;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.Date;

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
    public static final ImmutableKeyword<Boolean> CHECKPOINT = Keywords.keyword("checkpoint", Boolean.class);
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

    public Sequence<Record> crawl(URL url, RecordDefinition recordDefinition, String moreSelector, String checkpoint) throws Exception {
        Document document = document(url);
        Sequence<Record> recordsSoFar = crawl(document, recordDefinition, checkpoint);

        if (moreResults(moreSelector, document)) {
            return recordsSoFar.join(crawl(url(moreLink(moreSelector, document)), recordDefinition, moreSelector, checkpoint));
        }
       
        return recordsSoFar;
    }

    public Pair<String, Sequence<Record>> crawlAndReturnNewCheckpoint(URL url, RecordDefinition recordDefinition, String moreSelector, String checkpoint) throws Exception {
        Document document = document(url);
        Sequence<Record> recordsSoFar = crawl(document, recordDefinition, checkpoint);

        String newCheckpoint = evaluateNewCheckpoint(recordsSoFar, checkpoint);

        if (moreResults(moreSelector, document)) {
            return Pair.pair(newCheckpoint, recordsSoFar.join(crawl(url(moreLink(moreSelector, document)), recordDefinition, moreSelector, checkpoint)));
        }

        return Pair.pair(newCheckpoint, recordsSoFar);
    }

    private String evaluateNewCheckpoint(Sequence<Record> recordsSoFar, String oldCheckpoint) {
        if(recordsSoFar.isEmpty()) return oldCheckpoint;
        Option<Keyword> checkpoint = recordsSoFar.first().keywords().find(checkpoint());
        if(checkpoint.isEmpty()) return oldCheckpoint;
        return recordsSoFar.first().get(checkpoint.get()).toString();
    }

    private String moreLink(String moreSelector, Document document) {
        return selectContents(document, moreSelector);
    }

    private boolean moreResults(String more, Document document) {
        return !Strings.isEmpty(more) && !Strings.isEmpty(moreLink(more, document));
    }

    public Sequence<Record> crawl(URL url, RecordDefinition recordDefinition, String checkpoint) throws Exception {
        Document document = document(url);
        return crawl(document, recordDefinition, checkpoint);
    }

    public Sequence<Record> crawl(Document document, RecordDefinition recordDefinition, String checkpoint) throws Exception {
        XmlRecords xmlRecords = records(document);
        Sequence<Keyword> allFields = recordDefinition.fields();

        xmlRecords.define(recordDefinition.recordName(), allFields.toArray(Keyword.class));

        Sequence<Record> results = xmlRecords.get(recordDefinition.recordName());
        Sequence<Record> resultsAfterCheckpoint  = results.takeWhile(not(checkpointReached(checkpoint)));

        return allFields.filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                fold(resultsAfterCheckpoint, crawlSubFeeds()).realise();

    }

    private Predicate<? super Record> checkpointReached(final String checkpointValue) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                Sequence<Keyword> checkpoints = record.keywords().filter(checkpoint());
                return checkpoints.exists(checkpointValue(record, checkpointValue));
            }
        };
    }

    private Predicate<? super Keyword> checkpointValue(final Record record, final String checkpoint) {
        return new Predicate<Keyword>() {
            public boolean matches(Keyword keyword) {
                return record.get(keyword).equals(checkpoint);
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
                    return crawl(subFeed, subDefinitions, "").
                            map(merge(currentRecord));
                } catch (Exception e) {
                    return Sequences.sequence(currentRecord);
                }
            }
        };
    }

}
