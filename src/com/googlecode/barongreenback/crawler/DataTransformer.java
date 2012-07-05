package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class DataTransformer {
    private static final Document EMPTY_DOCUMENT = document("<empty/>");

    public static Sequence<Record> transformData(Document document, Definition source) {
        if(document == EMPTY_DOCUMENT) {
            return Sequences.empty();
        }
        return new DocumentFeeder().get(document, source).map(copy()).realise();
    }

    public static Document loadDocument(Response response) {
        String entity = response.entity().toString();
        if (entity.isEmpty()) {
            return EMPTY_DOCUMENT;
        }
        return document(entity);
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
}