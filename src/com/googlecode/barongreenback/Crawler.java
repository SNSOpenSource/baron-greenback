package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.xml.Xml;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import com.googlecode.utterlyidle.HttpHandler;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.handlers.ClientHttpHandler;

import java.net.URL;

import static com.googlecode.totallylazy.records.RecordCallables.merge;
import static com.googlecode.utterlyidle.RequestBuilder.get;


public class Crawler {
    public Records load(URL url) throws Exception {
        HttpHandler httpHandler = new ClientHttpHandler();
        Response response = httpHandler.handle(get(url.toString()).build());
        return new XmlRecords(Xml.load(new String(response.bytes())));
    }

    public Sequence<Record> crawl(XmlSource webSource) throws Exception {
        Records records = load(webSource.getUrl());
        records.define(webSource.getElement(), webSource.getFields());
        return records.get(webSource.getElement());
    }

    public Callable1<Record, Iterable<Record>> crawl(final Keyword<String> sourceUrl, final Keyword<Object> root, final Keyword<?>... fields) {
        return new Callable1<Record, Iterable<Record>>() {
            public Iterable<Record> call(Record record) throws Exception {
                String url = record.get(sourceUrl);
                XmlSource xmlSource = new XmlSource(new URL(url), root, fields);
                Sequence<Record> records = crawl(xmlSource);
                return records.map(merge(record));
            }
        };
    }
}
