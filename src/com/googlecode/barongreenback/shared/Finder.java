package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Record;

public interface Finder<T> {
    Sequence<T> find(Predicate<? super Record> predicate);
}
