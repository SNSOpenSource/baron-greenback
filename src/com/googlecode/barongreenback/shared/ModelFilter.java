package com.googlecode.barongreenback.shared;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Predicate;

import java.util.Iterator;
import java.util.Map;

public class ModelFilter {
    private final Predicate<String> keyPredicate;

    public ModelFilter(Predicate<String> keyPredicate) {
        this.keyPredicate = keyPredicate;
    }

    public Model filterModel(Model model) {
        Map<String, Object> map = filterMap(model.toMap());
        return Model.fromMap(map);
    }

    private Map<String, Object> filterMap(Map<String, Object> map) {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String nextKey = iterator.next();
            if (keyPredicate.matches(nextKey)) {
                if (map.get(nextKey) instanceof Map) {
                    map.put(nextKey, filterMap((Map<String, Object>) map.get(nextKey)));
                }
            } else {
                iterator.remove();
            }
        }
        return map;
    }
}
