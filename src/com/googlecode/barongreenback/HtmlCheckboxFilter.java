package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.iterators.PeekingIterator;

import java.util.ArrayList;
import java.util.List;

public class HtmlCheckboxFilter<T> {
    private final T negative;

    public HtmlCheckboxFilter(T negative) {
        this.negative = negative;
    }

    public Sequence<T> filter(Iterable<T> values) {
        List<T> result = new ArrayList<T>();
        for (PeekingIterator<T> iterator = new PeekingIterator<T>(values.iterator()); iterator.hasNext(); ) {
            T value = iterator.next();
            if(value.equals(negative)){
                result.add(value);
            }
            if (!value.equals(negative) && negative.equals(iterator.peek())) {
                result.add(value);
                iterator.next();
            }
        }
        return Sequences.sequence(result);
    }
}
