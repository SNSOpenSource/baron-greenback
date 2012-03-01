package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.xml.XmlRecords;
import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;

public class DocumentFeeder implements Feeder<Document> {
    public Sequence<Record> get(final Document document, RecordDefinition definition) {
        Records records = new XmlRecords(document);
        return records.get(definition.definition());
    }
}
