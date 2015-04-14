package com.googlecode.barongreenback.crawler;

import com.googlecode.barongreenback.shared.RecordDefinition;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Uri;

import static com.googlecode.barongreenback.shared.RecordDefinition.uniqueFields;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.SelectCallable.select;

public class DuplicateRemover implements Feeder<Uri> {
    private final Feeder<Uri> feeder;

    public DuplicateRemover(Feeder<Uri> feeder) {
        this.feeder = feeder;
    }

    public Sequence<Record> get(Uri source, RecordDefinition definition) throws Exception {
        final Sequence<Record> records = feeder.get(source, definition);
        return filterDuplicates(definition.definition(), records);
    }

    public static Sequence<Record> filterDuplicates(Definition definition, Sequence<Record> records) {
        final Sequence<Keyword<?>> unique = uniqueFields(definition);
        if (unique.isEmpty()) return records;
        return records.unique(select(unique.map(ignoreAlias())));
    }

    public static Callable1<Keyword<?>, Keyword<?>> ignoreAlias() {
        return new Callable1<Keyword<?>, Keyword<?>>() {
            public Keyword call(Keyword<?> keyword) throws Exception {
                return keyword(keyword.name(), keyword.forClass()).metadata(keyword.metadata());
            }
        };
    }
}
