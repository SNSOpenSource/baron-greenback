package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.googlecode.funclate.Model.mutable.model;
import static com.googlecode.totallylazy.Sequences.sequence;

public class ModelCleaner {
    private final Predicate<String> keyPredicate;

    public ModelCleaner(Predicate<String> keyPredicate) {
        this.keyPredicate = keyPredicate;
    }

    public Model clean(Model model) {
        Map<String, Object> map = filterMap(model.toMap());
        return model(map);
    }

    private Map<String, Object> filterMap(Map<String, Object> map) {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String nextKey = iterator.next();
            if (keyPredicate.matches(nextKey)) {
                Object value = map.get(nextKey);
                map.put(nextKey, processValue(value));
            } else {
                iterator.remove();
            }
        }
        return map;
    }

    private Object processValue(Object value) {
        if (value instanceof Map) {
            return filterMap((Map<String, Object>) value);
        } else if (value instanceof List) {
            return sequence((List)value).map(processValue()).toList();
        }
        return value;
    }

    private Callable1 processValue() {
        return new Callable1() {
            public Object call(Object value) throws Exception {
                return processValue(value);
            }
        };
    }
}
