package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class DataTransformer {
    public static Sequence<Record> transformData(Document document, Definition source) {
        return new DocumentFeeder().get(document, source).map(copy()).realise();
    }

    public static Document loadDocument(Response response) {
        String entity = response.entity().toString();
        if (entity.isEmpty()) {
            return document("<empty/>");
        }
        return document(entity);
    }

    public static Function1<Response, Document> loadDocument() {
        return new Function1<Response, Document>() {
            @Override
            public Document call(Response response) throws Exception {
                return loadDocument(response);
            }
        };
    }

    public static Record copy(Record record) {
        return Record.constructors.record(record.fields());
    }

    public static Function1<Record, Record> copy() {
        return new Function1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return copy(record);
            }
        };
    }

    public static Function1<Document, Sequence<Record>> transformData(final Definition source) {
        return new Function1<Document, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Document document) throws Exception {
                return transformData(document, source);
            }
        };
    }
}
