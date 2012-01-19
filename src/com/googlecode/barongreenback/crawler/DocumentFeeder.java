package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.MapRecord;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.xml.XmlRecords;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;

public class DocumentFeeder implements Feeder<Document> {
    public Sequence<Record> get(final Document document, RecordDefinition definition) {
        Records records = new XmlRecords(document);
        records.define(definition.recordName(), definition.fields().map(handleAlias()).toArray(Keyword.class));
        return records.get(definition.recordName()).map(replaceAlias(definition.fields()));
    }

    @SuppressWarnings("unchecked")
    public static Callable1<Record, Record> replaceAlias(final Sequence<Keyword<?>> fields) {
        return new Callable1<Record, Record>() {
            public Record call(Record record) throws Exception {
                Record result = MapRecord.record();
                for (Keyword field : fields) {
                    Keyword cleanKey = Keywords.keyword(field.name(), field.forClass()).metadata(field.metadata());
                    Object value = field.call(record);
                    result.set(cleanKey, value);
                }
                return result;
            }
        };
    }

    public static Callable1<Keyword, Keyword> handleAlias() {
        return new Callable1<Keyword, Keyword>() {
            public Keyword call(Keyword keyword) throws Exception {
                if (keyword instanceof AliasedKeyword) {
                    return ((AliasedKeyword) keyword).source();
                }
                return keyword;
            }
        };
    }
}
