package com.googlecode.barongreenback.shared;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.iterators.PeekingIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Sequences.splitOn;

public class CheckboxValues extends Sequence<Boolean>{
    private static final String negative = "false";
    private final Sequence<String> values;

    public CheckboxValues(Iterable<String> values) {
        this.values = sequence(values);
    }


    public Iterator<Boolean> iterator() {
        return filter().map(Strings.asBoolean()).iterator();
    }

    private Sequence<String> filter() {
        List<String> result = new ArrayList<String>();
        for (PeekingIterator<String> iterator = new PeekingIterator<String>(values.iterator()); iterator.hasNext(); ) {
            String value = iterator.next();
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
