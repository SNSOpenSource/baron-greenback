package com.googlecode.barongreenback.crawler;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import static com.googlecode.barongreenback.shared.RecordDefinition.priority;
import static com.googlecode.totallylazy.Sequences.reduce;
import static com.googlecode.totallylazy.comparators.NullComparator.Direction.Up;
import static com.googlecode.totallylazy.comparators.NullComparator.compare;

public class PriorityMerge {
    public static Sequence<Record> priorityMergeBy(Sequence<Record> records, Keyword<?> keyword) {
        return records.groupBy(keyword).map(reduce(priorityMerge));
    }

    public static final Function2<Record, Record, Record> priorityMerge = new Function2<Record, Record, Record>() {
        @Override
        public Record call(Record a, Record b) throws Exception {
            return priorityMerge(a, b);
        }
    };

    public static Function1<Record, Record> priorityMerge(final Record parent) {
        return priorityMerge.apply(parent);
    }

    private static Record priorityMerge(Record parent, Record child) {
        return parent.fields().fold(child, updateValues());
    }

    public static Function2<Record, Pair<Keyword<?>, Object>, Record> updateValues() {
        return new Function2<Record, Pair<Keyword<?>, Object>, Record>() {
            public Record call(Record child, Pair<Keyword<?>, Object> parentField) throws Exception {
                Keyword<?> parentKeyword = parentField.first();

                if (child.keywords().contains(parentKeyword) &&
                        compare(priority(childKeyword(child, parentKeyword)), priority(parentKeyword), Up) < 0)
                    return child;
                return child.set(Unchecked.<Keyword<Object>>cast(parentKeyword), parentField.second());
            }
        };
    }

    private static Keyword<Object> childKeyword(Record child, Keyword<?> parentKeyword) {
        return Keywords.matchKeyword(parentKeyword.name(), child.keywords());
    }
}
