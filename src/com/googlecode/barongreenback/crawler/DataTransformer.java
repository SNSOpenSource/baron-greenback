package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class DataTransformer {
    public static Sequence<Record> transformData(Document document, Definition source) {
        final Sequence<Record> data = new DocumentFeeder().get(document, source).map(copy()).realise();
        final Sequence<Record> records = DuplicateRemover.filterDuplicates(source, data);
        return records.realise();
    }

    public static Document loadDocument(Response response) {
        String entity = response.entity().toString();
        if (entity.isEmpty()) {
            return document("<empty/>");
        }
        return document(entity);
    }

    public static Record copy(Record record) {
        return Record.constructors.record(record.fields());
    }

    public static Function1<Record, Record> copy() {
        return  new Function1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return copy(record);
            }
        };
    }
}