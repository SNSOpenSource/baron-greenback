package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.Xml.functions.document;

public class DataTransformer {
    public static Sequence<Record> transformData(Option<Document> document, final Definition source) {
        return document.toSequence().flatMap(toDocumentFeeder(source));
    }

    public static Option<Document> loadDocument(Response response) {
        return one(response.entity().toString()).filter(not(empty())).map(document()).headOption();
    }

    private static Callable1<Document, Sequence<Record>> toDocumentFeeder(final Definition source) {
        return new Callable1<Document, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Document document) throws Exception {
                return new DocumentFeeder().get(document, source).map(copy()).realise();
            }
        };
    }

    private static Record copy(Record record) {
        return Record.constructors.record(record.fields());
    }

    private static Function1<Record, Record> copy() {
        return new Function1<Record, Record>() {
            @Override
            public Record call(Record record) throws Exception {
                return copy(record);
            }
        };
    }
}