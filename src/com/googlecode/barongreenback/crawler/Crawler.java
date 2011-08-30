package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.totallylazy.records.xml.mappings.DateMapping;
import com.googlecode.totallylazy.records.xml.mappings.Mappings;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.net.URL;
import java.util.Date;

import static com.googlecode.barongreenback.shared.RecordDefinition.RECORD_DEFINITION;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.RecordMethods.merge;
import static com.googlecode.utterlyidle.RequestBuilder.get;


public class Crawler {
    public XmlRecords load(URL url) throws Exception {
        HttpHandler httpHandler = new ClientHttpHandler();
        Response response = httpHandler.handle(get(url.toString()).build());
        String xml = new String(response.bytes());
        return new XmlRecords(Xml.document(xml), new Mappings().add(Date.class, DateMapping.atomDateFormat()));
    }

    public Sequence<Record> crawl(URL url, RecordDefinition recordDefinition) throws Exception {
        XmlRecords xmlRecords = load(url);
        Sequence<Keyword> allFields = recordDefinition.fields();

        xmlRecords.define(recordDefinition.recordName(), allFields.toArray(Keyword.class));

        Sequence<Record> result = xmlRecords.get(recordDefinition.recordName());

        return allFields.filter(where(metadata(RECORD_DEFINITION), is(notNullValue()))).
                fold(result, crawlSubFeeds()).realise();

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
                    return crawl(subFeed, subDefinitions).
                            map(merge(currentRecord));
                } catch (Exception e) {
                    return Sequences.sequence(currentRecord);
                }
            }
        };
    }
}
