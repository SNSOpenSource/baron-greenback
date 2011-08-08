package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Sequences.splitOn;

public class HtmlCheckboxFilter<T> {
    private final T negative;

    public HtmlCheckboxFilter(T negative) {
        this.negative = negative;
    }

    public Sequence<T> filter(Iterable<T> values) {
        Sequence<Sequence<T>> result = sequence(values).recursive(splitOn(negative));
        return result.isEmpty() ? sequence(negative) : result.map(emptyTo(negative));
    }

    private Callable1<? super Sequence<T>, T> emptyTo(final T negative) {
        return new Callable1<Sequence<T>, T>() {
            public T call(Sequence<T> sequence) throws Exception {
                if(sequence.isEmpty()){
                    return negative;
                }
                return sequence.head();
            }
        };
    }
}
