package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.totallylazy.records.xml.mappings.DateMapping;
import com.googlecode.totallylazy.records.xml.mappings.Mappings;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.net.URL;
import java.util.Date;

import static com.googlecode.barongreenback.XmlDefinition.XML_DEFINITION;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.URLs.url;
import static com.googlecode.totallylazy.records.Keywords.metadata;
import static com.googlecode.totallylazy.records.RecordMethods.merge;
import static com.googlecode.utterlyidle.RequestBuilder.get;


public class Crawler {
    public Records load(URL url) throws Exception {
        HttpHandler httpHandler = new ClientHttpHandler();
        Response response = httpHandler.handle(get(url.toString()).build());
        String xml = new String(response.bytes());
        return new XmlRecords(Xml.load(xml), new Mappings().add(Date.class, DateMapping.atomDateFormat()));
    }

    public Sequence<Record> crawl(URL url, XmlDefinition xmlDefinition) throws Exception {
        Records records = load(url);
        Sequence<Keyword> allFields = xmlDefinition.allFields();

        records.define(xmlDefinition.rootXPath(), allFields.toArray(Keyword.class));

        Sequence<Record> result = records.get(xmlDefinition.rootXPath());

        return allFields.filter(where(metadata(XML_DEFINITION), is(notNullValue()))).fold(result, new Callable2<Sequence<Record>, Keyword, Sequence<Record>>() {
            public Sequence<Record> call(Sequence<Record> resultsSoFar, Keyword keyword) throws Exception {
                return resultsSoFar.flatMap(crawl(keyword));
            }
        });

    }

    public Callable1<Record, Iterable<Record>> crawl(final Keyword sourceUrl) {
        return new Callable1<Record, Iterable<Record>>() {
            public Iterable<Record> call(Record record) throws Exception {
                Object feed = record.get(sourceUrl);
                Sequence<Record> records = crawl(url(feed.toString()), sourceUrl.metadata().get(XML_DEFINITION));
                return records.map(merge(record));
            }
        };
    }
}
