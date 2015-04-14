package com.googlecode.barongreenback.persistence.lucene;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Value;

public class NameBasedIndexFacetingPolicy implements Value<Predicate<String>> {
    private final Predicate<String> predicate;

    public NameBasedIndexFacetingPolicy(Predicate<String> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Predicate<String> value() {
        return predicate;
    }
}
