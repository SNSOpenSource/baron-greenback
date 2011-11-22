package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;
import com.googlecode.totallylazy.records.xml.XmlRecords;
import org.w3c.dom.Document;

import static com.googlecode.utterlyidle.RequestBuilder.get;

public class DocumentFeeder implements Feeder<Document> {
    public Sequence<Record> get(final Document document, RecordDefinition definition) {
        Records records = new XmlRecords(document);
        records.define(definition.recordName(), definition.fields().toArray(Keyword.class));
        return records.get(definition.recordName());
    }
}
