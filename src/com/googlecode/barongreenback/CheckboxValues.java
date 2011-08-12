package com.googlecode.barongreenback;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;

import java.util.Iterator;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Sequences.splitOn;

public class CheckboxValues extends Sequence<Boolean>{
    private static final String negative = "false";
    private final Iterable<String> values;

    public CheckboxValues(Iterable<String> values) {
        this.values = values;
    }

    private Callable1<? super Sequence<String>, String> emptyTo(final String negative) {
        return new Callable1<Sequence<String>, String>() {
            public String call(Sequence<String> sequence) throws Exception {
                if(sequence.isEmpty()){
                    return negative;
                }
                return sequence.head();
            }
        };
    }

    public Iterator<Boolean> iterator() {
        return filter().map(Strings.asBoolean()).iterator();
    }

    private Sequence<String> filter() {
        Sequence<Sequence<String>> result = sequence(values).recursive(Sequences.<String>splitOn(negative));
        return result.isEmpty() ? sequence(negative) : result.map(emptyTo(negative));
    }
}
