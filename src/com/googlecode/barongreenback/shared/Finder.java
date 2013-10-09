package com.googlecode.barongreenback.shared;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

public interface Finder<T> {
    Sequence<T> find(Predicate<? super Record> predicate);
}
