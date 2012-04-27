package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordsReader;
import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;

public class DocumentFeeder implements Feeder<Document> {
    public Sequence<Record> get(final Document document, RecordDefinition definition) {
        RecordsReader records = new ForwardOnlyXmlRecordsReader(document);
        return records.get(definition.definition()).interruptable();
    }
}
