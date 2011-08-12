package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
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

import static com.googlecode.totallylazy.URLs.url;
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
        records.define(xmlDefinition.rootXPath(), xmlDefinition.allFields().toArray(Keyword.class));
        return records.get(xmlDefinition.rootXPath());
    }

    public Callable1<Record, Iterable<Record>> crawl(final Keyword<String> sourceUrl, final Keyword<Object> root, final Keyword<?>... fields) {
        return new Callable1<Record, Iterable<Record>>() {
            public Iterable<Record> call(Record record) throws Exception {
                String feed = record.get(sourceUrl);
                XmlDefinition xmlDefinition = new XmlDefinition(root, Sequences.<Keyword>sequence(fields), Sequences.<Keyword>empty());
                Sequence<Record> records = crawl(url(feed), xmlDefinition);
                return records.map(merge(record));
            }
        };
    }
}
