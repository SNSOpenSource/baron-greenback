package com.googlecode.barongreenback.views;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;
import com.googlecode.totallylazy.records.Records;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.records.Keywords.keyword;
import static com.googlecode.totallylazy.records.MapRecord.record;
import static com.googlecode.totallylazy.records.RecordMethods.update;
import static com.googlecode.totallylazy.records.Using.using;

public class Views {
    private final Records records;
    public static final Keyword RECORDS_NAME = Keywords.keyword("views");
    public static final Keyword<String> VIEW_NAME = Keywords.keyword("name", String.class);
    public static final Keyword<String> FIELD_NAME = Keywords.keyword("fieldName", String.class);
    public static final Keyword<String> FIELD_TYPE = Keywords.keyword("fieldType", String.class);
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);

    public Views(Records records) {
        this.records = records;
        records.define(RECORDS_NAME, VIEW_NAME, FIELD_NAME, FIELD_TYPE, Keywords.UNIQUE, VISIBLE);
    }

    public Views add(View view) {
        records.add(RECORDS_NAME, asRecords(view));
        return this;
    }

    private Sequence<Record> asRecords(View view) {
        return view.fields().map(asRecord(view.name()));
    }

    private Callable1<? super Keyword, Record> asRecord(final Keyword view) {
        return new Callable1<Keyword, Record>() {
            public Record call(Keyword keyword) throws Exception {
                return record().set(VIEW_NAME, view.name()).
                        set(FIELD_NAME, keyword.name()).
                        set(FIELD_TYPE, keyword.forClass().getName()).
                        set(Keywords.UNIQUE, keyword.metadata().get(Keywords.UNIQUE)).
                        set(VISIBLE, keyword.metadata().get(VISIBLE));
            }
        };
    }

    public Sequence<View> get() {
        return records.get(RECORDS_NAME).
                groupBy(VIEW_NAME).
                map(asView());
    } 

    public Sequence<View> get(final Predicate<? super Record> predicate) {
        return records.get(RECORDS_NAME).
                filter(predicate).
                groupBy(VIEW_NAME).
                map(asView());
    }

    public Option<View> get(final String viewName) {
        return get(where(VIEW_NAME, is(viewName))).
                headOption();
    }

    private Callable1<? super Group<String, Record>, View> asView() {
        return new Callable1<Group<String, Record>, View>() {
            public View call(Group<String, Record> group) throws Exception {
                return View.view(keyword(group.key())).withFields(group.map(asField()));
            }
        };
    }

    private Callable1<? super Record, Keyword> asField() {
        return new Callable1<Record, Keyword>() {
            public Keyword call(Record record) throws Exception {
                return keyword(record.get(FIELD_NAME), Class.forName(record.get(FIELD_TYPE))).metadata(MapRecord.record().
                        set(Keywords.UNIQUE, record.get(Keywords.UNIQUE)).
                        set(VISIBLE, record.get(VISIBLE)));
            }
        };
    }


    public boolean contains(Keyword<Object> recordName) {
        return !get(recordName.name()).isEmpty();
    }

    public Views put(View view) {
        records.put(RECORDS_NAME, update(using(VIEW_NAME, FIELD_NAME), asRecords(view)));
        return this;
    }
}
