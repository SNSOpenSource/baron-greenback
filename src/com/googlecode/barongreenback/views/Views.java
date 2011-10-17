package com.googlecode.barongreenback.views;

import com.googlecode.barongreenback.shared.ModelRepository;
import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Keywords;
import com.googlecode.totallylazy.records.MapRecord;
import com.googlecode.totallylazy.records.Record;

import java.util.List;
import java.util.UUID;

import static com.googlecode.barongreenback.shared.ModelRepository.ID;
import static com.googlecode.barongreenback.views.View.view;
import static com.googlecode.funclate.Model.model;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.records.Keywords.UNIQUE;
import static com.googlecode.totallylazy.records.Keywords.keyword;

public class Views {
    public static final Keyword RECORDS_NAME = Keywords.keyword("views");
    public static final Keyword<String> VIEW_NAME = Keywords.keyword("name", String.class);
    public static final Keyword<Integer> FIELD_ORDER = Keywords.keyword("fieldOrder", Integer.class);
    public static final Keyword<String> FIELD_NAME = Keywords.keyword("fieldName", String.class);
    public static final Keyword<String> FIELD_TYPE = Keywords.keyword("fieldType", String.class);
    public static final Keyword<Boolean> VISIBLE = Keywords.keyword("visible", Boolean.class);
    public static final Keyword<String> GROUP = Keywords.keyword("group", String.class);
    private final ModelRepository modelRepository;

    public Views(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public Sequence<View> get() {
        return get(where(ID, is(notNullValue())));
    }

    public Sequence<View> get(final Predicate<? super Record> predicate) {
        return modelRepository.find(predicate).map(toView());
    }

    public Option<View> get(final String viewName) {
        return get(where(VIEW_NAME, is(viewName))).
                headOption();
    }

    private Callable1<? super Pair<UUID, Model>, View> toView() {
        return new Callable1<Pair<UUID, Model>, View>() {
            public View call(Pair<UUID, Model> pair) throws Exception {
                UUID id = pair.first();
                Model view = pair.second().get("view", Model.class);
                return view(keyword(view.get("name", String.class))).fields(sequence(view.<List<Model>>get("fields")).map(toField()));
            }
        };
    }

    private Callable1<? super Model, Keyword> toField() {
        return new Callable1<Model, Keyword>() {
            public Keyword call(Model model) throws Exception {
                return keyword(model.<String>get("name"),
                        Class.forName(model.<String>get("type"))).
                        metadata(MapRecord.record().
                                set(Keywords.UNIQUE, model.<Boolean>get("unique")).
                                set(VISIBLE, model.<Boolean>get("visible")).
                                set(GROUP, model.<String>get("group")));
            }
        };
    }



//    private Sequence<Record> asRecords(View view) {
//        return view.fields().zipWithIndex().map(asRecord(view.name()));
//    }
//
//    private Callable1<? super Pair<Number, Keyword>, Record> asRecord(final Keyword view) {
//        return new Callable1<Pair<Number, Keyword>, Record>() {
//            public Record call(Pair<Number, Keyword> pair) throws Exception {
//                Keyword keyword = pair.second();
//                return record().set(VIEW_NAME, view.name()).
//                        set(FIELD_ORDER, pair.first().intValue()).
//                        set(FIELD_NAME, keyword.name()).
//                        set(FIELD_TYPE, keyword.forClass().getName()).
//                        set(Keywords.UNIQUE, keyword.metadata().get(Keywords.UNIQUE)).
//                        set(VISIBLE, keyword.metadata().get(VISIBLE)).
//                        set(GROUP, keyword.metadata().get(GROUP));
//            }
//        };
//    }
//
//
//    private Callable1<? super Group<String, Record>, View> asView() {
//        return new Callable1<Group<String, Record>, View>() {
//            public View call(Group<String, Record> group) throws Exception {
//                return view(keyword(group.key())).fields(group.map(asField()));
//            }
//        };
//    }
//
//    private Callable1<? super Record, Keyword> asField() {
//        return new Callable1<Record, Keyword>() {
//            public Keyword call(Record record) throws Exception {
//                return keyword(record.get(FIELD_NAME), Class.forName(record.get(FIELD_TYPE))).metadata(MapRecord.record().
//                        set(Keywords.UNIQUE, record.get(Keywords.UNIQUE)).
//                        set(VISIBLE, record.get(VISIBLE)).
//                        set(GROUP, record.get(GROUP)));
//            }
//        };
//    }

    public boolean contains(Keyword<Object> recordName) {
        return !get(recordName.name()).isEmpty();
    }

    public Views put(UUID id, View view) {
        modelRepository.set(id, toModel(view));
        return this;
    }

    private Model toModel(View view) {
        return view.fields().fold(model().add("name", view.name().name()), new Callable2<Model, Keyword, Model>() {
            public Model call(Model view, Keyword keyword) throws Exception {
                Record metadata = keyword.metadata();
                return view.add("fields", model().
                        add("name", keyword.name()).
                        add("type", keyword.forClass().getName())).
                        add("unique", metadata.get(UNIQUE)).
                        add("visible", metadata.get(VISIBLE));

            }
        });
    }

    public void remove(UUID id) {
        modelRepository.remove(id);
    }
}
