package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.comparators.NullComparator;

import static com.googlecode.barongreenback.shared.RecordDefinition.priority;
import static com.googlecode.totallylazy.comparators.NullComparator.Direction.Up;
import static com.googlecode.totallylazy.comparators.NullComparator.compare;

public class PriorityMerge {
    public static Function1<Record, Record> priorityMerge(final Record parent) {
        return merge(parent.fields());
    }

    public static Function1<Record, Record> merge(final Sequence<Pair<Keyword<?>, Object>> parentFields) {
        return new Function1<Record, Record>() {
            public Record call(Record child) throws Exception {
                return parentFields.fold(child, updateValues());
            }
        };
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
