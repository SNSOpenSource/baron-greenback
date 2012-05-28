package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.utterlyidle.Response;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class DataExtractor {
    public static Sequence<Record> extractData(Response response, Definition source) {
        String entity = response.entity().toString();
        if (entity.isEmpty()) {
            return Sequences.empty();
        }
        Document document = document(entity);
        return new DocumentFeeder().get(document, source).map(copy()).realise();
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

    public static Function1<Response, Sequence<Record>> extractData(final Definition source) {
        return new Function1<Response, Sequence<Record>>() {
            @Override
            public Sequence<Record> call(Response response) throws Exception {
                return extractData(response, source);
            }
        };
    }
}
