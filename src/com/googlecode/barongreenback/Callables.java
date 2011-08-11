package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.records.Keyword;
import com.googlecode.totallylazy.records.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Callables.asString;

public class Callables {
    public static List<String> headersAsString(Sequence<Record> results) {
        return headers(results).map(asString()).toList();
    }

    public static Sequence<Keyword> headers(Sequence<Record> results) {
        return results.flatMap(keywords()).unique();
    }

    public static Callable1<? super Record, Sequence<Keyword>> keywords() {
        return new Callable1<Record, Sequence<Keyword>>() {
            public Sequence<Keyword> call(Record record) throws Exception {
                return record.keywords();
            }
        };
    }

    public static Callable1<? super Record, Map> asMap() {
        return new Callable1<Record, Map>() {
            public Map call(Record record) throws Exception {
                return record.fields().fold(new HashMap(), intoMap());
            }
        };
    }

    public static Callable2<? super Map, ? super Pair<Keyword, Object>, Map> intoMap() {
        return new Callable2<Map, Pair<Keyword, Object>, Map>() {
            public Map call(Map map, Pair<Keyword, Object> pair) throws Exception {
                map.put(pair.first().toString(), pair.second());
                return map;
            }
        };
    }

}
