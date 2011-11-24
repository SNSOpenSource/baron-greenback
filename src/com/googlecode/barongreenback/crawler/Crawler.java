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
import com.googlecode.totallylazy.Xml;
import com.googlecode.totallylazy.records.AliasedKeyword;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;
import com.googlecode.utterlyidle.handlers.HttpClient;
import com.googlecode.utterlyidle.handlers.PrintAuditor;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.Date;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.totallylazy.Callables.descending;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.merge;
import static com.googlecode.totallylazy.records.SelectCallable.select;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.Executors.newFixedThreadPool;


public class Crawler {
    public static final Keyword<URL> URL = keyword("url", URL.class);
    public static final Keyword<Document> DOCUMENT = keyword("document", Document.class);
    public static final Keyword<String> MORE = keyword("more", String.class);
    public static final Keyword<Boolean> CHECKPOINT = Keywords.keyword("checkpoint", Boolean.class);
    public static final Keyword<Date> CHECKPOINT_VALUE = keyword("checkpointValue", Date.class);
    public static final int DEFAULT_NUMBER_OF_THREADS = 10;

    private final HttpHandler httpClient;
    private final int numberOfCrawlerThreads;

    public Crawler() {
        this(new ClientHttpHandler());
    }

    public Crawler(HttpClient httpClient) {
        this.httpClient = new AuditHandler(httpClient, new PrintAuditor(System.out));
        numberOfCrawlerThreads = DEFAULT_NUMBER_OF_THREADS;
    }

    public Pair<Date, Sequence<Record>> crawl(Record crawlingDefinition) throws Exception {
        Document document = document(crawlingDefinition.get(URL));
        Pair<Sequence<Record>, Boolean> pair = crawlDocument(crawlingDefinition.set(DOCUMENT, document));
        Sequence<Record> newRecordsOnCurrentPage = pair.first();
        Boolean checkpointNotReached = pair.second();

        Date newCheckpoint = evaluateNewCheckpoint(newRecordsOnCurrentPage, crawlingDefinition.get(CHECKPOINT_VALUE));

        if (checkpointNotReached && moreResultsLink(crawlingDefinition.get(MORE), document)) {
            Sequence<Record> newRecordsOnPreviousPages = crawl(crawlingDefinition.set(URL, url(moreLink(crawlingDefinition.get(MORE), document)))).second();
            return Pair.pair(newCheckpoint, newRecordsOnCurrentPage.join(newRecordsOnPreviousPages));
        }

        return Pair.pair(newCheckpoint, newRecordsOnCurrentPage);
    }

    public Pair<Sequence<Record>, Boolean> crawlOnePageAtUrl(Record crawlingDefinition) throws Exception {
        Document document = document(crawlingDefinition.get(URL));
        return crawlDocument(crawlingDefinition.set(DOCUMENT, document));
    }

    public Pair<Sequence<Record>, Boolean> crawlDocument(Record documentCrawlingDefinition) throws Exception {
        RecordDefinition recordDefinition = documentCrawlingDefinition.get(RECORD_DEFINITION);
        Keyword<Object> recordName = recordDefinition.recordName();
        Sequence<Keyword> allFields = recordDefinition.fields();

        XmlRecords xmlRecords = records(documentCrawlingDefinition.get(DOCUMENT));
        xmlRecords.define(recordName, allFields.map(asSourceKeywords()).toArray(Keyword.class));

        Sequence<Keyword> uniqueFields = uniqueFields(recordDefinition);
        Sequence<Record> results = xmlRecords.get(recordName).map(select(allFields)).filter(unique(uniqueFields)).realise();
        Sequence<Record> sortedResults = sortResults(allFields, results);
        Sequence<Record> sortedResultsAfterCheckpoint = sortedResults.takeWhile(not(checkpointReached(documentCrawlingDefinition.get(CHECKPOINT_VALUE))));

        Sequence<Record> records = allFields.filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                fold(sortedResultsAfterCheckpoint, crawlSubFeeds()).realise();
        boolean checkpointNotReached = results.equals(sortedResultsAfterCheckpoint);
        return Pair.pair(records, checkpointNotReached);

    }

    private Predicate<Record> unique(final Sequence<Keyword> uniqueFields) {
        return new UniqueRecords(uniqueFields);
    }

    private Callable1<? super Keyword, Keyword> asSourceKeywords() {
        return new Callable1<Keyword, Keyword>() {
            public Keyword call(Keyword keyword) throws Exception {
                if(keyword instanceof AliasedKeyword){
                    return ((AliasedKeyword) keyword).source();
                }
                return keyword;
            }
        };
    }

    public XmlRecords records(Document document) throws Exception {
        return new XmlRecords(document);
    }

    private Sequence<Record> sortResults(Sequence<Keyword> headers, Sequence<Record> values) {
        Option<Keyword> checkpointKeyword = headers.find(checkpoint());
        if (!checkpointKeyword.isEmpty() && checkpointKeyword.get().forClass().equals(Date.class)) {
            return values.sortBy(descending(checkpointKeyword.get()));
        }
        return values;
    }

    private Document document(URL url) throws Exception {
        Response response = httpClient.handle(get(url.toString()).build());
        String xml = new String(response.bytes());
        return Xml.document(xml);
    }

    private Date evaluateNewCheckpoint(Sequence<Record> recordsSoFar, Date oldCheckpoint) {
        if (recordsSoFar.isEmpty()) return oldCheckpoint;
        Option<Keyword> checkpoint = recordsSoFar.first().keywords().find(checkpoint());
        if (checkpoint.isEmpty()) return oldCheckpoint;
        return (Date) recordsSoFar.first().get(checkpoint.get());
    }

    private String moreLink(String moreSelector, Document document) {
        return Xml.selectContents(document, moreSelector);
    }

    private boolean moreResultsLink(String more, Document document) {
        return !Strings.isEmpty(more) && !Strings.isEmpty(moreLink(more, document));
    }

    private Predicate<? super Record> checkpointReached(final Date checkpointValue) {
        return new Predicate<Record>() {
            public boolean matches(Record record) {
                Option<Keyword> checkpoint = record.keywords().filter(checkpoint()).headOption();
                if (!checkpoint.isEmpty() && checkpointValue != null) {
                    Date recordDate = (Date) record.get(checkpoint.get());
                    return recordDate.equals(checkpointValue) || recordDate.before(checkpointValue);
                }
                return false;
            }
        };
    }

    public static Predicate<? super Keyword> checkpoint() {
        return new Predicate<Keyword>() {
            public boolean matches(Keyword keyword) {
                return keyword.forClass().equals(Date.class) && TRUE.equals(keyword.metadata().get(CHECKPOINT));
            }
        };
    }

    private Callable2<Sequence<Record>, Keyword, Sequence<Record>> crawlSubFeeds() {
        return new Callable2<Sequence<Record>, Keyword, Sequence<Record>>() {
            public Sequence<Record> call(Sequence<Record> records, Keyword keyword) throws Exception {
                return records.flatMapConcurrently(crawl(keyword), newFixedThreadPool(numberOfCrawlerThreads));
            }
        };
    }

    private Callable1<Record, Iterable<Record>> crawl(final Keyword sourceUrl) {
        return new Callable1<Record, Iterable<Record>>() {
            public Iterable<Record> call(Record currentRecord) throws Exception {
                try {
                    URL subFeed = url(currentRecord.get(sourceUrl).toString());
                    RecordDefinition subDefinitions = sourceUrl.metadata().get(RECORD_DEFINITION);
                    return crawlOnePageAtUrl(record().set(URL, subFeed).set(RECORD_DEFINITION, subDefinitions)).first().
                            map(merge(currentRecord));
                } catch (Exception e) {
                    return Sequences.sequence(currentRecord);
                }
            }
        };
    }

}
